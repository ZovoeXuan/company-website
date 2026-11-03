package com.company.cms.media;

import com.company.cms.auth.CmsPrincipal;
import com.company.cms.common.ApiException;
import com.company.cms.common.ErrorCode;
import com.company.cms.system.AuditLogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 素材上传与删除。图片与视频都由文件头判断格式，不信任客户端扩展名与 Content-Type。
 * 图片上限 5 MB / 2000 万像素，视频上限 50 MB。
 */
@Service
public class MediaService {

    private static final long IMAGE_MAX_BYTES = 5L * 1024 * 1024;
    private static final long VIDEO_MAX_BYTES = 50L * 1024 * 1024;
    private static final long MAX_PIXELS = 20_000_000L;

    private final MediaRepository repository;
    private final MediaRefRepository refRepository;
    private final AuditLogService audit;
    private final Path root;

    public MediaService(MediaRepository repository, MediaRefRepository refRepository,
                        AuditLogService audit,
                        @Value("${cms.media.root:./data/media}") String rootDir) {
        this.repository = repository;
        this.refRepository = refRepository;
        this.audit = audit;
        this.root = Paths.get(rootDir).toAbsolutePath().normalize();
    }

    @Transactional
    public MediaItem upload(MultipartFile file, CmsPrincipal actor) {
        if (file == null || file.isEmpty()) {
            throw ApiException.validation("file", "请选择要上传的图片或视频");
        }

        ImageInspector.ImageInfo image = null;
        VideoInspector.VideoInfo video = null;
        try (InputStream in = file.getInputStream()) {
            byte[] head = in.readNBytes(4096);
            try {
                image = ImageInspector.inspect(head);
            } catch (IOException notImage) {
                try {
                    video = VideoInspector.inspect(head);
                } catch (IOException notVideo) {
                    throw new ApiException(ErrorCode.UNSUPPORTED_MEDIA,
                            "仅支持 JPG、PNG、WebP 图片或 MP4、WebM 视频");
                }
            }
        } catch (IOException ex) {
            throw new ApiException(ErrorCode.UNSUPPORTED_MEDIA, "文件读取失败");
        }

        String extension;
        String mimeType;
        String mediaType;
        int width = 0;
        int height = 0;
        Integer durationMs = null;

        if (image != null) {
            if (file.getSize() > IMAGE_MAX_BYTES) {
                throw new ApiException(ErrorCode.FILE_TOO_LARGE, "图片不能超过 5 MB");
            }
            if (image.width() < 1 || image.height() < 1) {
                throw new ApiException(ErrorCode.UNSUPPORTED_MEDIA, "图片尺寸无法识别");
            }
            if ((long) image.width() * image.height() > MAX_PIXELS) {
                throw new ApiException(ErrorCode.FILE_TOO_LARGE, "图片像素总数不能超过 2000 万");
            }
            extension = image.extension();
            mimeType = image.mimeType();
            mediaType = "IMAGE";
            width = image.width();
            height = image.height();
        } else {
            if (file.getSize() > VIDEO_MAX_BYTES) {
                throw new ApiException(ErrorCode.FILE_TOO_LARGE, "视频不能超过 50 MB");
            }
            extension = video.extension();
            mimeType = video.mimeType();
            mediaType = "VIDEO";
        }

        String storageKey = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path target = root.resolve(LocalDate.now(ZoneOffset.UTC).toString().substring(0, 7)
                .replace('-', '/')).resolve(storageKey);
        try {
            Files.createDirectories(target.getParent());
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "素材保存失败，请重试");
        }

        String originalName = safeName(file.getOriginalFilename());
        long id = repository.insert(storageKey, originalName, extension, mimeType, mediaType,
                file.getSize(), width, height, durationMs, actor.id());

        audit.success(actor.id(), "MEDIA_UPLOAD", "MEDIA", id,
                mediaType.equals("VIDEO")
                        ? "上传视频 " + extension + " " + humanSize(file.getSize())
                        : "上传图片 " + extension + " " + width + "x" + height);

        return new MediaItem(String.valueOf(id), originalName, mimeType, mediaType,
                width, height, durationMs, file.getSize(), publicUrl(id), null, false);
    }

    public record MediaPage(List<MediaItem> items, int page, int pageSize, long total) {
    }

    public MediaPage list(String keyword, boolean referencedOnly, String mediaType,
                          int page, int pageSize) {
        String type = null;
        if (mediaType != null && !mediaType.isBlank()) {
            type = mediaType.trim().toUpperCase(Locale.ROOT);
            if (!type.equals("IMAGE") && !type.equals("VIDEO")) {
                throw ApiException.validation("type", "素材类型只能是 IMAGE 或 VIDEO");
            }
        }
        int offset = (page - 1) * pageSize;
        List<MediaItem> items = repository.findPage(keyword, type, referencedOnly, offset, pageSize).stream()
                .map(row -> itemView(row.media(), row.referenced()))
                .toList();
        return new MediaPage(items, page, pageSize,
                repository.count(keyword, type, referencedOnly));
    }

    public MediaItem item(long id) {
        MediaRow media = require(id);
        return itemView(media, refRepository.existsByMedia(id));
    }

    private MediaItem itemView(MediaRow media, boolean referenced) {
        return new MediaItem(String.valueOf(media.id()), media.originalName(),
                media.mimeType(), media.mediaType(), media.width(), media.height(),
                media.durationMs(), media.byteSize(), publicUrl(media.id()),
                media.createdAt().atOffset(ZoneOffset.UTC).toString(), referenced);
    }

    @Transactional
    public void delete(long id, CmsPrincipal actor) {
        MediaRow media = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("素材不存在"));
        if (refRepository.existsByMedia(id)) {
            throw new ApiException(ErrorCode.REFERENCED, "该素材仍被内容引用，请先解除引用");
        }
        repository.logicalDelete(id);
        audit.success(actor.id(), "MEDIA_DELETE", "MEDIA", id, "删除素材 " + media.originalName());
    }

    public StoredFile load(long id) {
        MediaRow media = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("素材不存在"));
        Path path = resolvePath(media);
        if (!Files.exists(path)) {
            throw ApiException.notFound("素材文件不存在");
        }
        return new StoredFile(path, media.mimeType(), media.originalName());
    }

    /** 文件按 UTC 年/月分目录存放，路径由创建时间推导，不依赖客户端输入。 */
    private Path resolvePath(MediaRow media) {
        LocalDate created = media.createdAt().toLocalDate();
        return root.resolve(String.valueOf(created.getYear()))
                .resolve(String.format("%02d", created.getMonthValue()))
                .resolve(media.storageKey());
    }

    public MediaRow require(long id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("素材不存在"));
    }

    public boolean isPublic(long id) {
        return repository.isPubliclyReferenced(id);
    }

    private String publicUrl(long id) {
        return "/api/public/media/" + id;
    }

    private String humanSize(long bytes) {
        return bytes >= 1024 * 1024
                ? String.format(Locale.ROOT, "%.1f MB", bytes / 1024.0 / 1024.0)
                : Math.round(bytes / 1024.0) + " KB";
    }

    private String safeName(String name) {
        if (name == null || name.isBlank()) {
            return "media";
        }
        String trimmed = name.replaceAll("[\\\\/:*?\"<>|\\r\\n]", "_").trim();
        return trimmed.length() <= 200 ? trimmed : trimmed.substring(trimmed.length() - 200);
    }

    public record StoredFile(Path path, String mimeType, String originalName) {
    }
}

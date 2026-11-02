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
import java.util.UUID;

/**
 * 素材上传与删除。首版只收图片，格式由文件头判断，不信任客户端扩展名与 Content-Type。
 */
@Service
public class MediaService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;
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
            throw ApiException.validation("file", "请选择要上传的图片");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ApiException(ErrorCode.FILE_TOO_LARGE, "图片不能超过 5 MB");
        }

        ImageInspector.ImageInfo info;
        byte[] head;
        try (InputStream in = file.getInputStream()) {
            head = in.readNBytes(64);
            info = ImageInspector.inspect(head);
        } catch (IOException ex) {
            throw new ApiException(ErrorCode.UNSUPPORTED_MEDIA, "仅支持 JPG、PNG、WebP 图片");
        }

        if (info.width() < 1 || info.height() < 1) {
            throw new ApiException(ErrorCode.UNSUPPORTED_MEDIA, "图片尺寸无法识别");
        }
        if ((long) info.width() * info.height() > MAX_PIXELS) {
            throw new ApiException(ErrorCode.FILE_TOO_LARGE, "图片像素总数不能超过 2000 万");
        }

        String storageKey = UUID.randomUUID().toString().replace("-", "") + "." + info.extension();
        Path target = root.resolve(LocalDate.now(ZoneOffset.UTC).toString().substring(0, 7)
                .replace('-', '/')).resolve(storageKey);
        try {
            Files.createDirectories(target.getParent());
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "图片保存失败，请重试");
        }

        long id = repository.insert(storageKey, safeName(file.getOriginalFilename()), info.extension(),
                info.mimeType(), file.getSize(), info.width(), info.height(), actor.id());

        audit.success(actor.id(), "MEDIA_UPLOAD", "MEDIA", id,
                "上传图片 " + info.extension() + " " + info.width() + "x" + info.height());

        return new MediaItem(String.valueOf(id), safeName(file.getOriginalFilename()), info.mimeType(),
                info.width(), info.height(), file.getSize(), publicUrl(id), null, false);
    }

    public record MediaPage(List<MediaItem> items, int page, int pageSize, long total) {
    }

    public MediaPage list(String keyword, boolean referencedOnly, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<MediaItem> items = repository.findPage(keyword, referencedOnly, offset, pageSize).stream()
                .map(row -> new MediaItem(String.valueOf(row.media().id()), row.media().originalName(),
                        row.media().mimeType(), row.media().width(), row.media().height(),
                        row.media().byteSize(), publicUrl(row.media().id()),
                        row.media().createdAt().atOffset(ZoneOffset.UTC).toString(), row.referenced()))
                .toList();
        return new MediaPage(items, page, pageSize, repository.count(keyword, referencedOnly));
    }

    @Transactional
    public void delete(long id, CmsPrincipal actor) {
        MediaRow media = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("图片不存在"));
        if (refRepository.existsByMedia(id)) {
            throw new ApiException(ErrorCode.REFERENCED, "该图片仍被内容引用，请先解除引用");
        }
        repository.logicalDelete(id);
        audit.success(actor.id(), "MEDIA_DELETE", "MEDIA", id, "删除图片 " + media.originalName());
    }

    public StoredFile load(long id) {
        MediaRow media = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("图片不存在"));
        Path path = resolvePath(media);
        if (!Files.exists(path)) {
            throw ApiException.notFound("图片文件不存在");
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
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("图片不存在"));
    }

    public boolean isPublic(long id) {
        return repository.isPubliclyReferenced(id);
    }

    private String publicUrl(long id) {
        return "/api/public/media/" + id;
    }

    private String safeName(String name) {
        if (name == null || name.isBlank()) {
            return "image";
        }
        String trimmed = name.replaceAll("[\\\\/:*?\"<>|\\r\\n]", "_").trim();
        return trimmed.length() <= 200 ? trimmed : trimmed.substring(trimmed.length() - 200);
    }

    public record StoredFile(Path path, String mimeType, String originalName) {
    }
}

package com.company.cms.media;

import com.company.cms.auth.CmsPrincipal;
import com.company.cms.common.ApiResponse;
import com.company.cms.common.PageQuery;
import com.company.cms.common.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/media")
public class MediaController {

    private final MediaService mediaService;
    private final MediaRefRepository refRepository;

    public MediaController(MediaService mediaService, MediaRefRepository refRepository) {
        this.mediaService = mediaService;
        this.refRepository = refRepository;
    }

    @PostMapping
    public ApiResponse<MediaItem> upload(@RequestParam("file") MultipartFile file,
                                         @AuthenticationPrincipal CmsPrincipal actor,
                                         HttpServletRequest request) {
        return ApiResponse.ok(mediaService.upload(file, actor), RequestIdFilter.currentId(request));
    }

    @GetMapping
    public ApiResponse<MediaService.MediaPage> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "false") boolean referenced,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            HttpServletRequest request) {
        PageQuery query = PageQuery.of(page, pageSize, 24);
        return ApiResponse.ok(mediaService.list(keyword, referenced, query.page(), query.pageSize()),
                RequestIdFilter.currentId(request));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> file(@PathVariable long id) {
        MediaService.StoredFile file = mediaService.load(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.mimeType()))
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename*=UTF-8''" + encode(file.originalName()))
                .body(new FileSystemResource(file.path()));
    }

    @GetMapping("/{id}/references")
    public ApiResponse<List<Map<String, Object>>> references(@PathVariable long id,
                                                             HttpServletRequest request) {
        List<Map<String, Object>> data = refRepository.findByMedia(id).stream()
                .map(ref -> Map.<String, Object>of(
                        "sourceKind", ref.sourceKind(),
                        "sourceId", String.valueOf(ref.sourceId()),
                        "stage", ref.stage(),
                        "fieldPath", ref.fieldPath()))
                .toList();
        return ApiResponse.ok(data, RequestIdFilter.currentId(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable long id,
                                    @AuthenticationPrincipal CmsPrincipal actor,
                                    HttpServletRequest request) {
        mediaService.delete(id, actor);
        return ApiResponse.ok(null, RequestIdFilter.currentId(request));
    }

    private String encode(String name) {
        return java.net.URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
    }
}

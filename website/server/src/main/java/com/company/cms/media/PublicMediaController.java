package com.company.cms.media;

import com.company.cms.common.ApiException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * 公开图片。只有被「已发布且来源有效」的内容引用时才可访问，
 * 避免通过遍历 ID 拿到草稿里的图片。
 */
@RestController
@RequestMapping("/api/public/media")
public class PublicMediaController {

    private final MediaService mediaService;

    public PublicMediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> file(@PathVariable long id) {
        if (!mediaService.isPublic(id)) {
            throw ApiException.notFound("图片不存在");
        }
        MediaService.StoredFile file = mediaService.load(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.mimeType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .body(new FileSystemResource(file.path()));
    }
}

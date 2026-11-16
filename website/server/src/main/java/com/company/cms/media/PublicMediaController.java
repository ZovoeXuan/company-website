package com.company.cms.media;

import com.company.cms.common.ApiException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.CacheControl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Duration;

/**
 * 公开素材。只有被「已发布且来源有效」的内容引用时才可访问，
 * 避免通过遍历 ID 拿到草稿里的素材。
 * 使用短缓存 + must-revalidate（而非 365 天 immutable），确保内容下线撤回后公开访问立即失效。
 */
@RestController
@RequestMapping("/api/public/media")
public class PublicMediaController {

    private final MediaService mediaService;

    public PublicMediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/{id}")
    public void file(@PathVariable long id,
                     @RequestHeader(value = "Range", required = false) String range,
                     HttpServletResponse response) throws IOException {
        if (!mediaService.isPublic(id)) {
            throw ApiException.notFound("素材不存在");
        }
        MediaService.StoredFile file = mediaService.load(id);
        MediaHttp.write(response, file, range,
                CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic().mustRevalidate().getHeaderValue(), false);
    }
}

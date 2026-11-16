package com.company.cms.site;

import com.company.cms.common.ApiResponse;
import com.company.cms.common.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 公开站点信息、首页聚合与单页。未发布的配置一律 404，不泄露草稿。
 */
@RestController
@RequestMapping("/api/public")
public class PublicSiteConfigController {

    private final SiteConfigService service;
    private final HomeAggregateService homeService;
    private final PublicVersionService versionService;

    public PublicSiteConfigController(SiteConfigService service,
                                      HomeAggregateService homeService,
                                      PublicVersionService versionService) {
        this.service = service;
        this.homeService = homeService;
        this.versionService = versionService;
    }

    /** 轻量版本接口：支持 ETag / If-None-Match 304 协商，且不带中间层缓存。 */
    @GetMapping("/version")
    public ResponseEntity<ApiResponse<Map<String, Object>>> version(
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        PublicVersionService.VersionSnapshot snap = versionService.currentVersion();
        if (versionService.matchesIfNoneMatch(ifNoneMatch, snap.etag())) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .cacheControl(CacheControl.noStore())
                    .eTag(snap.etag())
                    .build();
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .eTag(snap.etag())
                .body(ApiResponse.ok(snap.toPayload(), RequestIdFilter.currentId(request)));
    }

    @GetMapping("/site-config")
    public ApiResponse<Map<String, Object>> siteConfig(HttpServletRequest request) {
        return ApiResponse.ok(service.publicSiteConfig(), RequestIdFilter.currentId(request));
    }

    /** 首页聚合：只返回首页自身的发布配置与解析好的卡片，不返回详情正文。 */
    @GetMapping("/home")
    public ApiResponse<Map<String, Object>> home(HttpServletRequest request) {
        return ApiResponse.ok(homeService.publicHome(), RequestIdFilter.currentId(request));
    }

    @GetMapping("/pages/{code}")
    public ApiResponse<Map<String, Object>> page(@PathVariable String code, HttpServletRequest request) {
        return ApiResponse.ok(service.publicPage(code), RequestIdFilter.currentId(request));
    }
}

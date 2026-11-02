package com.company.cms.site;

import com.company.cms.common.ApiResponse;
import com.company.cms.common.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    public PublicSiteConfigController(SiteConfigService service, HomeAggregateService homeService) {
        this.service = service;
        this.homeService = homeService;
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

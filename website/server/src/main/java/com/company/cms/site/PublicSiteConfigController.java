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
 * 公开站点信息与单页。未发布的配置一律 404，不泄露草稿。
 */
@RestController
@RequestMapping("/api/public")
public class PublicSiteConfigController {

    private final SiteConfigService service;

    public PublicSiteConfigController(SiteConfigService service) {
        this.service = service;
    }

    @GetMapping("/site-config")
    public ApiResponse<Map<String, Object>> siteConfig(HttpServletRequest request) {
        return ApiResponse.ok(service.publicSiteConfig(), RequestIdFilter.currentId(request));
    }

    @GetMapping("/pages/{code}")
    public ApiResponse<Map<String, Object>> page(@PathVariable String code, HttpServletRequest request) {
        return ApiResponse.ok(service.publicPage(code), RequestIdFilter.currentId(request));
    }
}

package com.company.cms.site;

import com.company.cms.auth.CmsPrincipal;
import com.company.cms.common.ApiResponse;
import com.company.cms.common.RequestIdFilter;
import com.company.cms.site.dto.ConfigDraftRequest;
import com.company.cms.site.dto.ConfigPublishRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 站点配置后台接口。键名白名单由枚举控制，不接受任意键。
 */
@RestController
@RequestMapping("/api/admin/configs")
public class AdminSiteConfigController {

    private final SiteConfigService service;

    public AdminSiteConfigController(SiteConfigService service) {
        this.service = service;
    }

    @GetMapping("/{key}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable String key, HttpServletRequest request) {
        return ApiResponse.ok(service.adminView(SiteConfigKey.parse(key)),
                RequestIdFilter.currentId(request));
    }

    @PutMapping("/{key}/draft")
    public ApiResponse<Map<String, Object>> saveDraft(@PathVariable String key,
                                                      @Valid @RequestBody ConfigDraftRequest body,
                                                      @AuthenticationPrincipal CmsPrincipal actor,
                                                      HttpServletRequest request) {
        return ApiResponse.ok(service.saveDraft(SiteConfigKey.parse(key), body.draft(),
                body.expectedLockVersion(), actor), RequestIdFilter.currentId(request));
    }

    @PostMapping("/{key}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> publish(@PathVariable String key,
                                     @Valid @RequestBody ConfigPublishRequest body,
                                     @AuthenticationPrincipal CmsPrincipal actor,
                                     HttpServletRequest request) {
        service.publish(SiteConfigKey.parse(key), body.expectedLockVersion(),
                body.expectedDraftVersion(), actor);
        return ApiResponse.ok(null, RequestIdFilter.currentId(request));
    }
}

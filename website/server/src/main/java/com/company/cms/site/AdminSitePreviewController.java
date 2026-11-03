package com.company.cms.site;

import com.company.cms.common.ApiResponse;
import com.company.cms.common.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 首页草稿预览：聚合结构与其公开接口完全一致，只是首屏与区块取草稿。
 * 与内容预览一样需要后台会话，不产生任何免登录地址。
 */
@RestController
@RequestMapping("/api/admin/preview")
public class AdminSitePreviewController {

    private final HomeAggregateService homeAggregate;

    public AdminSitePreviewController(HomeAggregateService homeAggregate) {
        this.homeAggregate = homeAggregate;
    }

    @GetMapping("/home")
    public ApiResponse<Map<String, Object>> home(HttpServletRequest request) {
        return ApiResponse.ok(homeAggregate.previewHome(), RequestIdFilter.currentId(request));
    }
}

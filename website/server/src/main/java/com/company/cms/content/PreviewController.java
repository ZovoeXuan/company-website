package com.company.cms.content;

import com.company.cms.common.ApiResponse;
import com.company.cms.common.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 后台预览：返回当前草稿，供管理站用与官网一致的组件渲染。
 * 预览不写入公开接口，也不改变发布状态；不提供免登录预览链接。
 */
@RestController
@RequestMapping("/api/admin/preview")
public class PreviewController {

    private final ContentService contentService;

    public PreviewController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/contents/{id}")
    public ApiResponse<Map<String, Object>> preview(@PathVariable long id, HttpServletRequest request) {
        return ApiResponse.ok(contentService.previewView(id), RequestIdFilter.currentId(request));
    }
}

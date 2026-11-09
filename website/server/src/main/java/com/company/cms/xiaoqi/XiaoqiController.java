package com.company.cms.xiaoqi;

import com.company.cms.auth.CmsPrincipal;
import com.company.cms.common.ApiResponse;
import com.company.cms.common.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class XiaoqiController {

    private final XiaoqiService service;

    public XiaoqiController(XiaoqiService service) {
        this.service = service;
    }

    /** 公开问答：匿名、免 CSRF，靠按 IP 限流与长度上限防滥用。 */
    @PostMapping("/api/public/xiaoqi/chat")
    public ApiResponse<Map<String, Object>> chat(@RequestBody XiaoqiService.ChatRequest body,
                                                 HttpServletRequest request) {
        String answer = service.chat(body.question(), body.history(), request.getRemoteAddr());
        return ApiResponse.ok(Map.of("answer", answer), RequestIdFilter.currentId(request));
    }

    @GetMapping("/api/admin/xiaoqi/integration")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> get(HttpServletRequest request) {
        return ApiResponse.ok(service.adminView(), RequestIdFilter.currentId(request));
    }

    @PutMapping("/api/admin/xiaoqi/integration")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> save(@RequestBody XiaoqiService.IntegrationRequest body,
                                                 @AuthenticationPrincipal CmsPrincipal actor,
                                                 HttpServletRequest request) {
        service.save(body, actor.id());
        return ApiResponse.ok(service.adminView(), RequestIdFilter.currentId(request));
    }

    @PostMapping("/api/admin/xiaoqi/integration/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> test(@AuthenticationPrincipal CmsPrincipal actor,
                                                 HttpServletRequest request) {
        return ApiResponse.ok(service.test(actor.id()), RequestIdFilter.currentId(request));
    }
}

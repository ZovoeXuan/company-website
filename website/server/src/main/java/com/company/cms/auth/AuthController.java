package com.company.cms.auth;

import com.company.cms.common.ApiResponse;
import com.company.cms.common.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 认证接口：CSRF 令牌、当前登录人、修改本人密码。
 * 登录与退出由 Spring Security 过滤器处理，见 SecurityConfig。
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 匿名可访问。返回原始令牌，由前端放到 X-CSRF-TOKEN 请求头。
     */
    @GetMapping("/csrf")
    public ApiResponse<Map<String, String>> csrf(CsrfToken token, HttpServletRequest request) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("token", token.getToken());
        data.put("headerName", token.getHeaderName());
        data.put("parameterName", token.getParameterName());
        return ApiResponse.ok(data, RequestIdFilter.currentId(request));
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(@AuthenticationPrincipal CmsPrincipal principal,
                                      HttpServletRequest request) {
        return ApiResponse.ok(MeResponse.of(principal), RequestIdFilter.currentId(request));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal CmsPrincipal principal,
                                            @Valid @RequestBody ChangePasswordRequest body,
                                            HttpServletRequest request) {
        authService.changePassword(principal, body.oldPassword(), body.newPassword());
        return ApiResponse.ok(null, RequestIdFilter.currentId(request));
    }
}

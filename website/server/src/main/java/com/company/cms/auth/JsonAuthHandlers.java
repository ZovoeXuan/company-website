package com.company.cms.auth;

import com.company.cms.common.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

/**
 * 登录成功、登录失败、退出的 JSON 处理。
 * 失败统一提示「账号或密码错误」，不区分账号不存在与密码错误。
 */
@Component
public class JsonAuthHandlers implements AuthenticationSuccessHandler, AuthenticationFailureHandler,
        LogoutSuccessHandler {

    private final JsonResponseWriter writer;
    private final AuthService authService;
    private final LoginRateLimiter rateLimiter;

    public JsonAuthHandlers(JsonResponseWriter writer, AuthService authService,
                            LoginRateLimiter rateLimiter) {
        this.writer = writer;
        this.authService = authService;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CmsPrincipal principal = (CmsPrincipal) authentication.getPrincipal();
        rateLimiter.onSuccess(LoginRateLimiter.key(principal.username(), request.getRemoteAddr()));
        authService.onLoginSuccess(principal);
        writer.writeOk(response, request, MeResponse.of(principal));
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String username = request.getParameter("username");
        String key = LoginRateLimiter.key(username, request.getRemoteAddr());

        if (exception instanceof SessionAuthenticationException) {
            writer.write(response, request, ErrorCode.UNAUTHENTICATED,
                    "会话创建失败，请重新登录");
            return;
        }

        rateLimiter.onFailure(key);
        Optional<Duration> locked = rateLimiter.lockedFor(key);
        if (locked.isPresent()) {
            long seconds = Math.max(1, locked.get().toSeconds());
            authService.onLoginFailure(username, "失败次数过多，已临时锁定");
            writer.writeRetryAfter(response, request, seconds);
            return;
        }

        String reason = exception instanceof DisabledException ? "账号已禁用"
                : exception instanceof BadCredentialsException ? "账号或密码错误"
                : "账号或密码错误";
        authService.onLoginFailure(username, reason);
        writer.write(response, request, ErrorCode.UNAUTHENTICATED, "账号或密码错误");
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {
        if (authentication != null && authentication.getPrincipal() instanceof CmsPrincipal principal) {
            authService.onLogout(principal);
            rateLimiter.onSuccess(LoginRateLimiter.key(principal.username(), request.getRemoteAddr()));
        }
        writer.writeOk(response, request, null);
    }
}

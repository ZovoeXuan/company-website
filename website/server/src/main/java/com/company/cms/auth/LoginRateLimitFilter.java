package com.company.cms.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

/**
 * 登录前置限流检查。放在认证过滤器之前，锁定期间直接返回 429，不再比对密码。
 * 失败次数的累加在认证失败处理器中完成。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/admin/auth/login";

    private final LoginRateLimiter rateLimiter;
    private final JsonResponseWriter writer;

    public LoginRateLimitFilter(LoginRateLimiter rateLimiter, JsonResponseWriter writer) {
        this.rateLimiter = rateLimiter;
        this.writer = writer;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod()) && LOGIN_PATH.equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String key = LoginRateLimiter.key(request.getParameter("username"), request.getRemoteAddr());
        Optional<Duration> locked = rateLimiter.lockedFor(key);
        if (locked.isPresent()) {
            long seconds = Math.max(1, locked.get().toSeconds());
            writer.writeRetryAfter(response, request, seconds);
            return;
        }
        filterChain.doFilter(request, response);
    }
}

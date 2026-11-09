package com.company.cms.config;

import com.company.cms.auth.CmsUserDetailsService;
import com.company.cms.auth.JsonAuthHandlers;
import com.company.cms.auth.JsonResponseWriter;
import com.company.cms.auth.LoginRateLimitFilter;
import com.company.cms.common.ErrorCode;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.io.IOException;

/**
 * 安全基线：
 * - 公开只读接口 /api/public/** 允许匿名；公开咨询提交单独排除 CSRF（阶段 6）
 * - 后台接口除 csrf / login 外均需登录，写操作需 CSRF
 * - 认证失败、越权、CSRF 失败都返回统一 JSON，不重定向到 HTML 页面
 * - 登录失败限流在认证过滤器之前检查
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String LOGIN_PATH = "/api/admin/auth/login";

    private final JsonResponseWriter writer;
    private final JsonAuthHandlers authHandlers;
    private final LoginRateLimitFilter rateLimitFilter;
    private final CmsUserDetailsService userDetailsService;

    public SecurityConfig(JsonResponseWriter writer, JsonAuthHandlers authHandlers,
                          LoginRateLimitFilter rateLimitFilter, CmsUserDetailsService userDetailsService) {
        this.writer = writer;
        this.authHandlers = authHandlers;
        this.rateLimitFilter = rateLimitFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepository.setHeaderName("X-CSRF-TOKEN");
        csrfRepository.setCookieCustomizer(cookie -> cookie.path("/").sameSite("Lax"));

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepository)
                        // 使用原始令牌而非 BREACH 掩码，前端从 /csrf 取到后原样放进请求头
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/api/public/leads", "/api/public/xiaoqi/chat"))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.ASYNC).permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/admin/auth/csrf", LOGIN_PATH).permitAll()
                        .requestMatchers("/api/admin/**").authenticated()
                        .anyRequest().denyAll())
                // 不生成默认 HTML 登录页：该地址只接受表单 POST
                .formLogin(form -> form
                        .loginPage(LOGIN_PATH)
                        .loginProcessingUrl(LOGIN_PATH)
                        .successHandler(authHandlers)
                        .failureHandler(authHandlers)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/api/admin/auth/logout")
                        .logoutSuccessHandler(authHandlers)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"))
                .httpBasic(AbstractHttpConfigurer::disable)
                .requestCache(RequestCacheConfigurer::disable)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(this::writeUnauthenticated)
                        .accessDeniedHandler(this::writeAccessDenied));
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        // 账号不存在与密码错误返回同一异常，避免枚举账号
        provider.setHideUserNotFoundExceptions(true);
        return provider;
    }

    private void writeUnauthenticated(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException ex) throws IOException {
        writer.write(response, request, ErrorCode.UNAUTHENTICATED, "登录状态已失效，请重新登录");
    }

    private void writeAccessDenied(HttpServletRequest request, HttpServletResponse response,
                                   AccessDeniedException ex) throws IOException {
        if (ex instanceof CsrfException) {
            writer.write(response, request, ErrorCode.CSRF_INVALID, ErrorCode.CSRF_INVALID.defaultMessage());
            return;
        }
        writer.write(response, request, ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
    }
}

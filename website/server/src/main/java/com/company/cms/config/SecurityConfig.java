package com.company.cms.config;

import com.company.cms.common.ApiResponse;
import com.company.cms.common.ErrorCode;
import com.company.cms.common.RequestIdFilter;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfException;

import java.io.IOException;

/**
 * 安全基线：
 * - 公开只读接口 /api/public/** 允许匿名
 * - 后台接口除 csrf / login 外均需登录
 * - 未登录与越权都返回统一 JSON，便于前端区分 401/403
 * - CSRF 保持开启，仅 POST /api/public/leads 在实现时单独排除
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.ASYNC).permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/admin/auth/csrf", "/api/admin/auth/login").permitAll()
                        .requestMatchers("/api/admin/**").authenticated()
                        .anyRequest().denyAll())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(RequestCacheConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(this::writeUnauthenticated)
                        .accessDeniedHandler(this::writeAccessDenied));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    private void writeUnauthenticated(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException ex) throws IOException {
        write(response, request, ErrorCode.UNAUTHENTICATED, "登录状态已失效，请重新登录");
    }

    private void writeAccessDenied(HttpServletRequest request, HttpServletResponse response,
                                   AccessDeniedException ex) throws IOException {
        if (ex instanceof CsrfException) {
            write(response, request, ErrorCode.CSRF_INVALID, ErrorCode.CSRF_INVALID.defaultMessage());
            return;
        }
        write(response, request, ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
    }

    private void write(HttpServletResponse response, HttpServletRequest request,
                       ErrorCode code, String message) throws IOException {
        response.setStatus(code.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiResponse<Void> body = ApiResponse.failure(code, message, null, RequestIdFilter.currentId(request));
        objectMapper.writeValue(response.getWriter(), body);
    }
}

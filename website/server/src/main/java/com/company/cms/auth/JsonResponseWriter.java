package com.company.cms.auth;

import com.company.cms.common.ApiResponse;
import com.company.cms.common.ErrorCode;
import com.company.cms.common.RequestIdFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一写出 JSON 响应。安全过滤器链中的响应不经过 Controller，必须复用同一结构。
 */
@Component
public class JsonResponseWriter {

    private final ObjectMapper objectMapper;

    public JsonResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletResponse response, HttpServletRequest request,
                      ErrorCode code, String message) throws IOException {
        write(response, request, code.status().value(),
                ApiResponse.failure(code, message, null, RequestIdFilter.currentId(request)));
    }

    public void writeOk(HttpServletResponse response, HttpServletRequest request, Object data) throws IOException {
        write(response, request, HttpServletResponse.SC_OK,
                ApiResponse.ok(data, RequestIdFilter.currentId(request)));
    }

    public void writeRetryAfter(HttpServletResponse response, HttpServletRequest request,
                                long seconds) throws IOException {
        response.setHeader("Retry-After", String.valueOf(seconds));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("retryAfterSeconds", seconds);
        write(response, request, ErrorCode.RATE_LIMITED.status().value(),
                ApiResponse.failure(ErrorCode.RATE_LIMITED,
                        "登录尝试过于频繁，请 " + seconds + " 秒后再试", null,
                        RequestIdFilter.currentId(request)));
    }

    private void write(HttpServletResponse response, HttpServletRequest request,
                       int status, ApiResponse<?> body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}

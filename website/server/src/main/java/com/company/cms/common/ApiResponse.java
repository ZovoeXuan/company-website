package com.company.cms.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 统一响应体。成功与失败共用同一结构，失败时同时使用正确的 HTTP 状态码。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        String code,
        String message,
        T data,
        List<FieldErrorItem> fieldErrors,
        String requestId
) {

    public static <T> ApiResponse<T> ok(T data, String requestId) {
        return new ApiResponse<>(ErrorCode.OK.name(), "success", data, null, requestId);
    }

    public static <T> ApiResponse<T> failure(ErrorCode code, String message,
                                             List<FieldErrorItem> fieldErrors, String requestId) {
        String text = (message == null || message.isBlank()) ? code.defaultMessage() : message;
        return new ApiResponse<>(code.name(), text, null, fieldErrors, requestId);
    }
}

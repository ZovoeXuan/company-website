package com.company.cms.common;

import org.springframework.http.HttpStatus;

/**
 * 统一错误码。HTTP 状态码与业务码的对应关系见开发文档 9.1。
 */
public enum ErrorCode {

    OK(HttpStatus.OK, "success"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "请检查提交内容"),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "请先登录"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "没有操作权限"),
    CSRF_INVALID(HttpStatus.FORBIDDEN, "请求校验失败，请刷新页面后重试"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "内容不存在或已下线"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "请求方法不支持"),
    VERSION_CONFLICT(HttpStatus.CONFLICT, "内容已被他人修改，请刷新后重试"),
    REFERENCED(HttpStatus.CONFLICT, "该内容仍被其他位置引用"),
    IDEMPOTENCY_CONFLICT(HttpStatus.CONFLICT, "重复提交的内容与首次不一致"),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "文件超出大小限制"),
    UNSUPPORTED_MEDIA(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "文件类型不支持"),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "操作过于频繁，请稍后再试"),
    FORM_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "表单暂不可用"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "服务异常，请稍后重试");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}

package com.company.cms.common;

import java.util.List;

/**
 * 业务异常。携带错误码与字段级错误，由统一异常处理器转换为响应。
 */
public class ApiException extends RuntimeException {

    private final ErrorCode code;
    private final List<FieldErrorItem> fieldErrors;

    public ApiException(ErrorCode code) {
        this(code, code.defaultMessage(), null);
    }

    public ApiException(ErrorCode code, String message) {
        this(code, message, null);
    }

    public ApiException(ErrorCode code, String message, List<FieldErrorItem> fieldErrors) {
        super(message);
        this.code = code;
        this.fieldErrors = fieldErrors;
    }

    public ErrorCode code() {
        return code;
    }

    public List<FieldErrorItem> fieldErrors() {
        return fieldErrors;
    }

    public static ApiException notFound(String message) {
        return new ApiException(ErrorCode.NOT_FOUND, message);
    }

    public static ApiException validation(String field, String message) {
        return new ApiException(ErrorCode.VALIDATION_ERROR, "请检查提交内容",
                List.of(new FieldErrorItem(field, message)));
    }

    public static ApiException forbidden(String message) {
        return new ApiException(ErrorCode.FORBIDDEN, message);
    }
}

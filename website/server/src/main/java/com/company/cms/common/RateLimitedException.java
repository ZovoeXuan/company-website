package com.company.cms.common;

/**
 * 限流异常：除 429 外还需要返回 Retry-After，供前端提示可重试时间。
 */
public class RateLimitedException extends ApiException {

    private final long retryAfterSeconds;

    public RateLimitedException(long retryAfterSeconds) {
        super(ErrorCode.RATE_LIMITED, "提交过于频繁，请 " + Math.max(1, retryAfterSeconds) + " 秒后重试");
        this.retryAfterSeconds = Math.max(1, retryAfterSeconds);
    }

    public long retryAfterSeconds() {
        return retryAfterSeconds;
    }
}

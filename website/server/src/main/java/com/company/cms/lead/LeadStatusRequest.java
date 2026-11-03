package com.company.cms.lead;

/**
 * 后台状态处理请求。expectedLockVersion 用于避免并发覆盖。
 */
public record LeadStatusRequest(String status, String note, Integer expectedLockVersion) {
}

package com.company.cms.system;

/**
 * 审计日志列表项。不包含任何敏感正文。
 */
public record AuditLogItem(
        String id,
        String actorName,
        String action,
        String targetType,
        String targetId,
        String result,
        String summary,
        String createdAt
) {
}

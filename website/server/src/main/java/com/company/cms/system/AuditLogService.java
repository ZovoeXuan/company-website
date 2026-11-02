package com.company.cms.system;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;

/**
 * 审计写入。成功业务操作与日志同事务提交（由调用方事务保证）。
 */
@Service
public class AuditLogService {

    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void success(Long actorId, String action, String targetType, Long targetId, String summary) {
        record(actorId, action, targetType, targetId, "SUCCESS", summary);
    }

    public void failure(Long actorId, String action, String targetType, Long targetId, String summary) {
        record(actorId, action, targetType, targetId, "FAILURE", summary);
    }

    private void record(Long actorId, String action, String targetType, Long targetId,
                        String result, String summary) {
        String requestId = MDC.get("requestId");
        repository.insert(actorId, action, targetType, targetId, null, null,
                requestId == null ? "unknown" : requestId, result, summary);
    }
}

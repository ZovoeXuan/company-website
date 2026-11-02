package com.company.cms.system;

import com.company.cms.common.ApiResponse;
import com.company.cms.common.PageQuery;
import com.company.cms.common.PageResult;
import com.company.cms.common.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 审计日志：仅 ADMIN 只读，不提供修改与删除接口。
 */
@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogRepository repository;

    public AuditLogController(AuditLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ApiResponse<PageResult<AuditLogItem>> list(
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            HttpServletRequest request) {

        PageQuery query = PageQuery.of(page, pageSize, 20);
        List<AuditLogItem> items = repository.findPage(actorId, action, from, to,
                query.offset(), query.pageSize());
        long total = repository.count(actorId, action, from, to);
        return ApiResponse.ok(PageResult.of(items, query.page(), query.pageSize(), total),
                RequestIdFilter.currentId(request));
    }
}

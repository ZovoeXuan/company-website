package com.company.cms.dashboard;

import com.company.cms.system.RoleCode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作台数据。按角色裁剪：编辑只能看到内容相关统计，不接触客户线索与账号。
 */
@Service
public class DashboardService {

    private final JdbcTemplate jdbc;

    public DashboardService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public DashboardResponse load(RoleCode role) {
        Map<String, Long> byType = new LinkedHashMap<>();
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT content_type, COUNT(*) AS total
                FROM cms_content
                WHERE deleted = 0
                GROUP BY content_type
                ORDER BY content_type
                """);
        for (Map<String, Object> row : rows) {
            byType.put(String.valueOf(row.get("content_type")),
                    ((Number) row.get("total")).longValue());
        }

        long published = count("""
                SELECT COUNT(*) FROM cms_content
                WHERE deleted = 0 AND publication_status = 'PUBLISHED'
                """);
        // 包含未发布草稿及已发布但存在未发布修改的内容（对齐文档 5.1 hasUnpublishedChanges 口径）
        long drafts = count("""
                SELECT COUNT(*) FROM cms_content
                WHERE deleted = 0
                  AND (published_version IS NULL OR draft_version <> published_version)
                """);

        if (role != RoleCode.ADMIN) {
            return new DashboardResponse(published, drafts, byType, null, null);
        }

        long pendingLeads = count("SELECT COUNT(*) FROM cms_lead WHERE status = 'NEW'");
        long userCount = count("SELECT COUNT(*) FROM sys_user WHERE enabled = 1");
        return new DashboardResponse(published, drafts, byType, pendingLeads, userCount);
    }

    private long count(String sql) {
        Long value = jdbc.queryForObject(sql, Long.class);
        return value == null ? 0L : value;
    }
}

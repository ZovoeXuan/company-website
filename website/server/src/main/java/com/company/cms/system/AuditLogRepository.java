package com.company.cms.system;

import com.company.cms.common.UtcTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 审计日志只追加、后台只读；summary 不允许写密码、令牌、手机号或正文。
 */
@Repository
public class AuditLogRepository {

    private static final RowMapper<AuditLogItem> ROW_MAPPER = (rs, rowNum) -> new AuditLogItem(
            String.valueOf(rs.getLong("id")),
            rs.getString("actor_name"),
            rs.getString("action"),
            rs.getString("target_type"),
            rs.getObject("target_id") == null ? null : String.valueOf(rs.getLong("target_id")),
            rs.getString("result"),
            rs.getString("summary"),
            rs.getObject("created_at", LocalDateTime.class).atOffset(java.time.ZoneOffset.UTC).toString()
    );

    private final JdbcTemplate jdbc;

    public AuditLogRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(Long actorId, String action, String targetType, Long targetId,
                       Integer draftVersion, Integer publishedVersion,
                       String requestId, String result, String summary) {
        jdbc.update("""
                        INSERT INTO sys_audit_log
                            (actor_id, action, target_type, target_id, draft_version, published_version,
                             request_id, result, summary, created_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                actorId, action, targetType, targetId, draftVersion, publishedVersion,
                requestId, result, truncate(summary), UtcTime.now());
    }

    public List<AuditLogItem> findPage(Long actorId, String action, LocalDate from, LocalDate to,
                                       int offset, int limit) {
        List<Object> args = new ArrayList<>();
        String where = buildWhere(actorId, action, from, to, args);

        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(limit);
        pageArgs.add(offset);

        return jdbc.query("""
                        SELECT a.*, u.display_name AS actor_name
                          FROM sys_audit_log a
                          LEFT JOIN sys_user u ON u.id = a.actor_id
                        """ + where + " ORDER BY a.created_at DESC, a.id DESC LIMIT ? OFFSET ?",
                ROW_MAPPER, pageArgs.toArray());
    }

    public long count(Long actorId, String action, LocalDate from, LocalDate to) {
        List<Object> args = new ArrayList<>();
        String where = buildWhere(actorId, action, from, to, args);
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM sys_audit_log a" + where,
                Long.class, args.toArray());
        return total == null ? 0 : total;
    }

    private String buildWhere(Long actorId, String action, LocalDate from, LocalDate to, List<Object> args) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        if (actorId != null) {
            where.append(" AND a.actor_id = ?");
            args.add(actorId);
        }
        if (action != null && !action.isBlank()) {
            where.append(" AND a.action = ?");
            args.add(action.trim());
        }
        if (from != null) {
            where.append(" AND a.created_at >= ?");
            args.add(from.atStartOfDay());
        }
        if (to != null) {
            where.append(" AND a.created_at < ?");
            args.add(to.plusDays(1).atStartOfDay());
        }
        return where.toString();
    }

    private String truncate(String summary) {
        if (summary == null) {
            return "";
        }
        return summary.length() <= 500 ? summary : summary.substring(0, 500);
    }
}

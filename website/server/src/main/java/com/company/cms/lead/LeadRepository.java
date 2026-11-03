package com.company.cms.lead;

import com.company.cms.common.UtcTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * cms_lead 数据访问。表由 V1 迁移建立，咨询不套用内容发布模型。
 */
@Repository
public class LeadRepository {

    private final JdbcTemplate jdbc;

    public LeadRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record LeadRow(
            long id,
            String receiptNo,
            String idempotencyKey,
            byte[] payloadHash,
            int payloadHashVersion,
            String name,
            String companyName,
            byte[] phoneCiphertext,
            String phoneLast4,
            String demand,
            String sourcePath,
            String sourcePosition,
            String contentType,
            Long contentId,
            int consentVersion,
            LocalDateTime consentAt,
            String status,
            String lastNote,
            Long handledBy,
            LocalDateTime handledAt,
            int lockVersion,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String contentTitle,
            String handlerName
    ) {
    }

    private final RowMapper<LeadRow> mapper = (rs, rowNum) -> new LeadRow(
            rs.getLong("id"),
            rs.getString("receipt_no"),
            rs.getString("idempotency_key"),
            rs.getBytes("payload_hash"),
            rs.getInt("payload_hash_version"),
            rs.getString("name"),
            rs.getString("company_name"),
            rs.getBytes("phone_ciphertext"),
            rs.getString("phone_last4"),
            rs.getString("demand"),
            rs.getString("source_path"),
            rs.getString("source_position"),
            rs.getString("content_type"),
            rs.getObject("content_id") == null ? null : rs.getLong("content_id"),
            rs.getInt("consent_version"),
            rs.getObject("consent_at", LocalDateTime.class),
            rs.getString("status"),
            rs.getString("last_note"),
            rs.getObject("handled_by") == null ? null : rs.getLong("handled_by"),
            rs.getObject("handled_at", LocalDateTime.class),
            rs.getInt("lock_version"),
            rs.getObject("created_at", LocalDateTime.class),
            rs.getObject("updated_at", LocalDateTime.class),
            rs.getString("content_title"),
            rs.getString("handler_name")
    );

    private static final String SELECT_WITH_JOIN = """
            SELECT l.*, c.published_title AS content_title, u.display_name AS handler_name
              FROM cms_lead l
              LEFT JOIN cms_content c ON c.id = l.content_id
              LEFT JOIN sys_user u ON u.id = l.handled_by
            """;

    public Optional<LeadRow> findByIdempotencyKey(String key) {
        List<LeadRow> rows = jdbc.query(SELECT_WITH_JOIN + " WHERE l.idempotency_key = ?", mapper, key);
        return rows.stream().findFirst();
    }

    public Optional<LeadRow> findById(long id) {
        List<LeadRow> rows = jdbc.query(SELECT_WITH_JOIN + " WHERE l.id = ?", mapper, id);
        return rows.stream().findFirst();
    }

    public long insert(String receiptNo, String idempotencyKey, byte[] payloadHash, int payloadHashVersion,
                       String name, String companyName, byte[] phoneCiphertext, String phoneLast4,
                       String demand, String sourcePath, String sourcePosition, String contentType,
                       Long contentId, int consentVersion, LocalDateTime consentAt) {
        LocalDateTime now = UtcTime.now();
        String sql = """
                INSERT INTO cms_lead
                    (receipt_no, idempotency_key, payload_hash, payload_hash_version,
                     name, company_name, phone_ciphertext, phone_last4, demand, source_path,
                     source_position, content_type, content_id, consent_version, consent_at,
                     status, lock_version, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'NEW', 0, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, receiptNo);
            ps.setString(2, idempotencyKey);
            ps.setBytes(3, payloadHash);
            ps.setInt(4, payloadHashVersion);
            ps.setString(5, name);
            ps.setString(6, companyName);
            ps.setBytes(7, phoneCiphertext);
            ps.setString(8, phoneLast4);
            ps.setString(9, demand);
            ps.setString(10, sourcePath);
            ps.setString(11, sourcePosition);
            ps.setString(12, contentType);
            ps.setObject(13, contentId);
            ps.setInt(14, consentVersion);
            ps.setObject(15, consentAt);
            ps.setObject(16, now);
            ps.setObject(17, now);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("创建咨询未返回主键");
        }
        return key.longValue();
    }

    public List<LeadRow> findPage(String status, String keyword, LocalDate from, LocalDate to,
                                  int offset, int limit) {
        List<Object> args = new ArrayList<>();
        String where = buildWhere(status, keyword, from, to, args);
        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(limit);
        pageArgs.add(offset);
        return jdbc.query(SELECT_WITH_JOIN + where + " ORDER BY l.created_at DESC, l.id DESC LIMIT ? OFFSET ?",
                mapper, pageArgs.toArray());
    }

    public long count(String status, String keyword, LocalDate from, LocalDate to) {
        List<Object> args = new ArrayList<>();
        String where = buildWhere(status, keyword, from, to, args);
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM cms_lead l" + where,
                Long.class, args.toArray());
        return total == null ? 0 : total;
    }

    private String buildWhere(String status, String keyword, LocalDate from, LocalDate to,
                              List<Object> args) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        if (status != null && !status.isBlank()) {
            where.append(" AND l.status = ?");
            args.add(status.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (l.name LIKE ? OR l.company_name LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }
        if (from != null) {
            where.append(" AND l.created_at >= ?");
            args.add(from.atStartOfDay());
        }
        if (to != null) {
            where.append(" AND l.created_at < ?");
            args.add(to.plusDays(1).atStartOfDay());
        }
        return where.toString();
    }

    public int updateStatus(long id, String status, String note, long actorId, int expectedLockVersion) {
        LocalDateTime now = UtcTime.now();
        return jdbc.update("""
                        UPDATE cms_lead
                           SET status = ?, last_note = COALESCE(?, last_note),
                               handled_by = ?, handled_at = ?,
                               lock_version = lock_version + 1, updated_at = ?
                         WHERE id = ? AND lock_version = ?
                        """,
                status, note, actorId, now, now, id, expectedLockVersion);
    }
}

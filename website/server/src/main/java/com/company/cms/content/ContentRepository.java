package com.company.cms.content;

import com.company.cms.common.UtcTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * cms_content 数据访问。草稿、发布快照、投影列都由本类维护。
 */
@Repository
public class ContentRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public ContentRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    private final RowMapper<ContentRow> mapper = (rs, rowNum) -> new ContentRow(
            rs.getLong("id"),
            ContentType.valueOf(rs.getString("content_type")),
            readJson(rs.getString("draft_json")),
            readJson(rs.getString("published_json")),
            PublicationStatus.valueOf(rs.getString("publication_status")),
            rs.getInt("draft_version"),
            intOrNull(rs, "published_version"),
            rs.getInt("lock_version"),
            rs.getString("published_title"),
            rs.getObject("published_category_id") == null ? null : rs.getLong("published_category_id"),
            intOrNull(rs, "published_sort_order"),
            rs.getObject("published_display_time", LocalDateTime.class),
            rs.getObject("published_at", LocalDateTime.class),
            rs.getInt("deleted") == 1
    );

    /**
     * MySQL 的 INT UNSIGNED 通过 JDBC 返回 Long，不能直接强转 Integer。
     */
    private static Integer intOrNull(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        Object value = rs.getObject(column);
        return value == null ? null : ((Number) value).intValue();
    }

    private JsonNode readJson(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return objectMapper.readTree(raw);
    }

    private String writeJson(JsonNode node) {
        return node == null ? "{}" : objectMapper.writeValueAsString(node);
    }

    public Optional<ContentRow> findById(long id) {
        List<ContentRow> rows = jdbc.query(
                "SELECT * FROM cms_content WHERE id = ? AND deleted = 0", mapper, id);
        return rows.stream().findFirst();
    }

    public long insert(ContentType type, JsonNode draft, Long actorId) {
        String sql = """
                INSERT INTO cms_content
                    (content_type, draft_json, publication_status, draft_version, lock_version,
                     created_at, updated_at, created_by, updated_by)
                VALUES (?, ?, 'UNPUBLISHED', 1, 0, ?, ?, ?, ?)
                """;
        LocalDateTime now = UtcTime.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, type.name());
            ps.setString(2, writeJson(draft));
            ps.setObject(3, now);
            ps.setObject(4, now);
            ps.setObject(5, actorId);
            ps.setObject(6, actorId);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("创建内容未返回主键");
        }
        return key.longValue();
    }

    /**
     * 保存草稿：draft_version 与 lock_version 同时自增，版本不匹配返回 0。
     */
    public int updateDraft(long id, JsonNode draft, int expectedLockVersion, Long actorId) {
        return jdbc.update("""
                        UPDATE cms_content
                           SET draft_json = ?, draft_version = draft_version + 1,
                               lock_version = lock_version + 1, updated_at = ?, updated_by = ?
                         WHERE id = ? AND lock_version = ? AND deleted = 0
                        """,
                writeJson(draft), UtcTime.now(), actorId, id, expectedLockVersion);
    }

    /**
     * 发布：把草稿整份复制为快照，同时写入投影列。
     * 条件包含 lock_version 与 draft_version，避免用旧草稿发布。
     */
    public int publish(long id, String title, Long categoryId, int sortOrder,
                       LocalDateTime displayTime, int expectedLockVersion,
                       int expectedDraftVersion, Long actorId) {
        LocalDateTime now = UtcTime.now();
        return jdbc.update("""
                        UPDATE cms_content
                           SET published_json = draft_json,
                               published_title = ?,
                               published_category_id = ?,
                               published_sort_order = ?,
                               published_display_time = ?,
                               publication_status = 'PUBLISHED',
                               published_version = draft_version,
                               published_at = ?,
                               published_by = ?,
                               lock_version = lock_version + 1,
                               updated_at = ?
                         WHERE id = ? AND lock_version = ? AND draft_version = ? AND deleted = 0
                        """,
                title, categoryId, sortOrder, displayTime, now, actorId, now,
                id, expectedLockVersion, expectedDraftVersion);
    }

    public int offline(long id, int expectedLockVersion, Long actorId) {
        return jdbc.update("""
                        UPDATE cms_content
                           SET publication_status = 'OFFLINE',
                               lock_version = lock_version + 1, updated_at = ?, updated_by = ?
                         WHERE id = ? AND lock_version = ? AND publication_status = 'PUBLISHED'
                           AND deleted = 0
                        """,
                UtcTime.now(), actorId, id, expectedLockVersion);
    }

    public int logicalDelete(long id, int expectedLockVersion) {
        return jdbc.update("""
                        UPDATE cms_content
                           SET deleted = 1, lock_version = lock_version + 1, updated_at = ?
                         WHERE id = ? AND lock_version = ? AND deleted = 0
                           AND publication_status <> 'PUBLISHED'
                        """,
                UtcTime.now(), id, expectedLockVersion);
    }

    public record AdminFilter(ContentType type, String keyword, Long categoryId,
                              PublicationStatus status, Boolean unpublishedChanges) {
    }

    public List<ContentRow> findPage(AdminFilter filter, int offset, int limit) {
        List<Object> args = new ArrayList<>();
        String where = buildWhere(filter, args);
        args.add(limit);
        args.add(offset);
        return jdbc.query("SELECT * FROM cms_content" + where
                + " ORDER BY updated_at DESC, id DESC LIMIT ? OFFSET ?", mapper, args.toArray());
    }

    public long count(AdminFilter filter) {
        List<Object> args = new ArrayList<>();
        String where = buildWhere(filter, args);
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM cms_content" + where, Long.class, args.toArray());
        return total == null ? 0 : total;
    }

    private String buildWhere(AdminFilter filter, List<Object> args) {
        StringBuilder where = new StringBuilder(" WHERE deleted = 0");
        if (filter.type() != null) {
            where.append(" AND content_type = ?");
            args.add(filter.type().name());
        }
        if (filter.categoryId() != null) {
            where.append(" AND published_category_id = ?");
            args.add(filter.categoryId());
        }
        if (filter.status() != null) {
            where.append(" AND publication_status = ?");
            args.add(filter.status().name());
        }
        if (Boolean.TRUE.equals(filter.unpublishedChanges())) {
            where.append(" AND (publication_status <> 'PUBLISHED' OR published_version IS NULL"
                    + " OR draft_version > published_version)");
        }
        if (filter.keyword() != null && !filter.keyword().isBlank()) {
            String pattern = "%" + filter.keyword().trim()
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            where.append(" AND (published_title LIKE ? OR draft_json ->> '$.title' LIKE ?)");
            args.add(pattern);
            args.add(pattern);
        }
        return where.toString();
    }

    // ---- 公开查询：只认已发布且未删除 ----

    public List<ContentRow> findPublished(ContentType type, Long categoryId, String keyword,
                                          boolean newsOrder, int offset, int limit) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT * FROM cms_content
                 WHERE content_type = ? AND publication_status = 'PUBLISHED' AND deleted = 0
                """);
        args.add(type.name());
        if (categoryId != null) {
            sql.append(" AND published_category_id = ?");
            args.add(categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND published_title LIKE ?");
            args.add("%" + keyword.trim()
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%");
        }
        sql.append(newsOrder
                ? " ORDER BY published_display_time DESC, id DESC"
                : " ORDER BY published_sort_order ASC, id DESC");
        sql.append(" LIMIT ? OFFSET ?");
        args.add(limit);
        args.add(offset);
        return jdbc.query(sql.toString(), mapper, args.toArray());
    }

    public long countPublished(ContentType type, Long categoryId, String keyword) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*) FROM cms_content
                 WHERE content_type = ? AND publication_status = 'PUBLISHED' AND deleted = 0
                """);
        args.add(type.name());
        if (categoryId != null) {
            sql.append(" AND published_category_id = ?");
            args.add(categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND published_title LIKE ?");
            args.add("%" + keyword.trim()
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%");
        }
        Long total = jdbc.queryForObject(sql.toString(), Long.class, args.toArray());
        return total == null ? 0 : total;
    }

    public Optional<ContentRow> findPublishedById(long id) {
        List<ContentRow> rows = jdbc.query("""
                SELECT * FROM cms_content
                 WHERE id = ? AND publication_status = 'PUBLISHED' AND deleted = 0
                """, mapper, id);
        return rows.stream().findFirst();
    }

    public Optional<ContentRow> findAdjacentNews(long id, boolean previous) {
        String sql = previous
                ? """
                  SELECT * FROM cms_content
                   WHERE content_type = 'NEWS' AND publication_status = 'PUBLISHED' AND deleted = 0
                     AND (published_display_time, id) < (SELECT published_display_time, id FROM cms_content WHERE id = ?)
                   ORDER BY published_display_time DESC, id DESC LIMIT 1
                  """
                : """
                  SELECT * FROM cms_content
                   WHERE content_type = 'NEWS' AND publication_status = 'PUBLISHED' AND deleted = 0
                     AND (published_display_time, id) > (SELECT published_display_time, id FROM cms_content WHERE id = ?)
                   ORDER BY published_display_time ASC, id ASC LIMIT 1
                  """;
        List<ContentRow> rows = jdbc.query(sql, mapper, id);
        return rows.stream().findFirst();
    }

    public boolean existsPublishedCategory(Long categoryId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM cms_category WHERE id = ? AND publication_status = 'PUBLISHED'"
                        + " AND deleted = 0", Long.class, categoryId);
        return count != null && count > 0;
    }
}

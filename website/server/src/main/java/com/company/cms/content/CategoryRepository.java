package com.company.cms.content;

import com.company.cms.common.PageQuery;
import com.company.cms.common.PageResult;
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
 * cms_category 数据访问。单层分类，发布时把名称与排序写入投影列，
 * 并发发布同名分类由唯一键 (category_type, active_published_name) 兜底。
 */
@Repository
public class CategoryRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public CategoryRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    private final RowMapper<CategoryRow> mapper = (rs, rowNum) -> new CategoryRow(
            rs.getLong("id"),
            ContentType.valueOf(rs.getString("category_type")),
            readJson(rs.getString("draft_json")),
            readJson(rs.getString("published_json")),
            PublicationStatus.valueOf(rs.getString("publication_status")),
            rs.getInt("draft_version"),
            intOrNull(rs, "published_version"),
            rs.getInt("lock_version"),
            rs.getString("published_name"),
            intOrNull(rs, "published_sort_order"),
            rs.getInt("deleted") == 1
    );

    /** MySQL 的 INT UNSIGNED 通过 JDBC 返回 Long，不能直接强转 Integer。 */
    private static Integer intOrNull(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        Object value = rs.getObject(column);
        return value == null ? null : ((Number) value).intValue();
    }

    private JsonNode readJson(String raw) {
        return raw == null || raw.isBlank() ? null : objectMapper.readTree(raw);
    }

    private String writeJson(JsonNode node) {
        return node == null ? "{}" : objectMapper.writeValueAsString(node);
    }

    public Optional<CategoryRow> findById(long id) {
        List<CategoryRow> rows = jdbc.query(
                "SELECT * FROM cms_category WHERE id = ? AND deleted = 0", mapper, id);
        return rows.stream().findFirst();
    }

    public long insert(ContentType type, JsonNode draft, Long actorId) {
        String sql = """
                INSERT INTO cms_category
                    (category_type, draft_json, publication_status, draft_version, lock_version,
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
            throw new IllegalStateException("创建分类未返回主键");
        }
        return key.longValue();
    }

    public int updateDraft(long id, JsonNode draft, int expectedLockVersion, Long actorId) {
        return jdbc.update("""
                        UPDATE cms_category
                           SET draft_json = ?, draft_version = draft_version + 1,
                               lock_version = lock_version + 1, updated_at = ?, updated_by = ?
                         WHERE id = ? AND lock_version = ? AND deleted = 0
                        """,
                writeJson(draft), UtcTime.now(), actorId, id, expectedLockVersion);
    }

    public int publish(long id, String name, int sortOrder, int expectedLockVersion,
                       int expectedDraftVersion, Long actorId) {
        LocalDateTime now = UtcTime.now();
        return jdbc.update("""
                        UPDATE cms_category
                           SET published_json = draft_json,
                               published_name = ?,
                               published_sort_order = ?,
                               publication_status = 'PUBLISHED',
                               published_version = draft_version,
                               published_at = ?, published_by = ?,
                               lock_version = lock_version + 1, updated_at = ?
                         WHERE id = ? AND lock_version = ? AND draft_version = ? AND deleted = 0
                        """,
                name, sortOrder, now, actorId, now, id, expectedLockVersion, expectedDraftVersion);
    }

    public int offline(long id, int expectedLockVersion, Long actorId) {
        return jdbc.update("""
                        UPDATE cms_category
                           SET publication_status = 'OFFLINE',
                               lock_version = lock_version + 1, updated_at = ?, updated_by = ?
                         WHERE id = ? AND lock_version = ? AND publication_status = 'PUBLISHED'
                           AND deleted = 0
                        """,
                UtcTime.now(), actorId, id, expectedLockVersion);
    }

    public int logicalDelete(long id, int expectedLockVersion) {
        return jdbc.update("""
                        UPDATE cms_category
                           SET deleted = 1, lock_version = lock_version + 1, updated_at = ?
                         WHERE id = ? AND lock_version = ? AND deleted = 0
                           AND publication_status <> 'PUBLISHED'
                        """,
                UtcTime.now(), id, expectedLockVersion);
    }

    public long countPublishedContents(Long categoryId) {
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM cms_content
                 WHERE published_category_id = ? AND publication_status = 'PUBLISHED' AND deleted = 0
                """, Long.class, categoryId);
        return count == null ? 0 : count;
    }

    public long countDraftReferences(Long categoryId) {
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM cms_content
                 WHERE deleted = 0
                   AND CAST(draft_json ->> '$.categoryId' AS UNSIGNED) = ?
                """, Long.class, categoryId);
        return count == null ? 0 : count;
    }

    public List<CategoryRow> findPage(ContentType type, String keyword, int offset, int limit) {
        List<Object> args = new ArrayList<>();
        String where = buildWhere(type, keyword, args);
        args.add(limit);
        args.add(offset);
        return jdbc.query("SELECT * FROM cms_category" + where
                + " ORDER BY updated_at DESC, id DESC LIMIT ? OFFSET ?", mapper, args.toArray());
    }

    public long count(ContentType type, String keyword) {
        List<Object> args = new ArrayList<>();
        String where = buildWhere(type, keyword, args);
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM cms_category" + where, Long.class, args.toArray());
        return total == null ? 0 : total;
    }

    private String buildWhere(ContentType type, String keyword, List<Object> args) {
        StringBuilder where = new StringBuilder(" WHERE deleted = 0");
        if (type != null) {
            where.append(" AND category_type = ?");
            args.add(type.name());
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (published_name LIKE ? OR draft_json ->> '$.name' LIKE ?)");
            String pattern = "%" + keyword.trim()
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            args.add(pattern);
            args.add(pattern);
        }
        return where.toString();
    }

    public List<CategoryRow> findPublished(ContentType type) {
        return jdbc.query("""
                SELECT * FROM cms_category
                 WHERE category_type = ? AND publication_status = 'PUBLISHED' AND deleted = 0
                 ORDER BY published_sort_order ASC, id ASC
                """, mapper, type.name());
    }

    public PageResult<CategoryRow> page(ContentType type, String keyword, PageQuery query) {
        List<CategoryRow> items = findPage(type, keyword, query.offset(), query.pageSize());
        return PageResult.of(items, query.page(), query.pageSize(), count(type, keyword));
    }
}

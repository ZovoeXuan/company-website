package com.company.cms.media;

import com.company.cms.common.UtcTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MediaRepository {

    private final JdbcTemplate jdbc;

    public MediaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<MediaRow> mapper = (rs, rowNum) -> new MediaRow(
            rs.getLong("id"),
            rs.getString("storage_key"),
            rs.getString("original_name"),
            rs.getString("extension"),
            rs.getString("mime_type"),
            rs.getLong("byte_size"),
            rs.getInt("width"),
            rs.getInt("height"),
            rs.getObject("created_at", java.time.LocalDateTime.class)
    );

    public long insert(String storageKey, String originalName, String extension, String mimeType,
                       long byteSize, int width, int height, long uploadedBy) {
        String sql = """
                INSERT INTO cms_media
                    (storage_key, original_name, extension, mime_type, byte_size, width, height,
                     uploaded_by, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, storageKey);
            ps.setString(2, originalName);
            ps.setString(3, extension);
            ps.setString(4, mimeType);
            ps.setLong(5, byteSize);
            ps.setInt(6, width);
            ps.setInt(7, height);
            ps.setLong(8, uploadedBy);
            ps.setObject(9, UtcTime.now());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("上传未返回主键");
        }
        return key.longValue();
    }

    public Optional<MediaRow> findById(long id) {
        List<MediaRow> rows = jdbc.query(
                "SELECT * FROM cms_media WHERE id = ? AND deleted = 0", mapper, id);
        return rows.stream().findFirst();
    }

    /** 首页聚合批量取媒体，避免每张图单独查询。 */
    public List<MediaRow> findByIds(java.util.Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        return jdbc.query("SELECT * FROM cms_media WHERE deleted = 0 AND id IN (" + placeholders + ")",
                mapper, ids.toArray());
    }

    public record MediaListItem(MediaRow media, boolean referenced) {
    }

    public List<MediaListItem> findPage(String keyword, boolean referencedOnly, int offset, int limit) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT m.*, EXISTS(SELECT 1 FROM cms_media_ref r WHERE r.media_id = m.id) AS referenced
                  FROM cms_media m
                 WHERE m.deleted = 0
                """);
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND m.original_name LIKE ?");
            args.add("%" + keyword.trim()
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%");
        }
        if (referencedOnly) {
            sql.append(" AND EXISTS(SELECT 1 FROM cms_media_ref r WHERE r.media_id = m.id)");
        }
        sql.append(" ORDER BY m.created_at DESC, m.id DESC LIMIT ? OFFSET ?");
        args.add(limit);
        args.add(offset);

        return jdbc.query(sql.toString(), (rs, rowNum) -> new MediaListItem(
                mapper.mapRow(rs, rowNum),
                rs.getBoolean("referenced")
        ), args.toArray());
    }

    public long count(String keyword, boolean referencedOnly) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM cms_media m WHERE m.deleted = 0");
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND m.original_name LIKE ?");
            args.add("%" + keyword.trim()
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%");
        }
        if (referencedOnly) {
            sql.append(" AND EXISTS(SELECT 1 FROM cms_media_ref r WHERE r.media_id = m.id)");
        }
        Long total = jdbc.queryForObject(sql.toString(), Long.class, args.toArray());
        return total == null ? 0 : total;
    }

    public int logicalDelete(long id) {
        return jdbc.update("UPDATE cms_media SET deleted = 1 WHERE id = ? AND deleted = 0", id);
    }

    /**
     * 公开访问必须至少有一条「已发布且来源有效」的引用，避免通过遍历 ID 拿到未发布素材。
     */
    public boolean isPubliclyReferenced(long id) {
        Boolean exists = jdbc.queryForObject("""
                SELECT EXISTS(
                    SELECT 1 FROM cms_media_ref r
                     WHERE r.media_id = ? AND r.stage = 'PUBLISHED'
                       AND (
                         (r.source_kind = 'CONTENT' AND EXISTS(
                             SELECT 1 FROM cms_content c
                              WHERE c.id = r.source_id AND c.publication_status = 'PUBLISHED' AND c.deleted = 0))
                         OR (r.source_kind = 'CATEGORY' AND EXISTS(
                             SELECT 1 FROM cms_category g
                              WHERE g.id = r.source_id AND g.publication_status = 'PUBLISHED' AND g.deleted = 0))
                         OR (r.source_kind = 'CONFIG' AND EXISTS(
                             SELECT 1 FROM cms_config cf
                              WHERE cf.id = r.source_id AND cf.publication_status = 'PUBLISHED' AND cf.deleted = 0))
                       )
                )
                """, Boolean.class, id);
        return Boolean.TRUE.equals(exists);
    }
}

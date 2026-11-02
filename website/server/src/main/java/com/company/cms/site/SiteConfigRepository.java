package com.company.cms.site;

import com.company.cms.common.UtcTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * cms_config 数据访问。六个单例只能改草稿与发布，不提供删除或整体下线。
 */
@Repository
public class SiteConfigRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public SiteConfigRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    private final RowMapper<SiteConfigRow> mapper = (rs, rowNum) -> new SiteConfigRow(
            SiteConfigKey.parse(rs.getString("config_key")),
            readJson(rs.getString("draft_json")),
            readJson(rs.getString("published_json")),
            com.company.cms.content.PublicationStatus.valueOf(rs.getString("publication_status")),
            rs.getInt("draft_version"),
            intOrNull(rs, "published_version"),
            rs.getInt("lock_version"),
            rs.getObject("published_at", LocalDateTime.class)
    );

    private static Integer intOrNull(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        Object value = rs.getObject(column);
        return value == null ? null : ((Number) value).intValue();
    }

    private JsonNode readJson(String raw) {
        return raw == null || raw.isBlank() ? null : objectMapper.readTree(raw);
    }

    public Optional<SiteConfigRow> find(SiteConfigKey key) {
        List<SiteConfigRow> rows = jdbc.query(
                "SELECT * FROM cms_config WHERE config_key = ? AND deleted = 0", mapper, key.name());
        return rows.stream().findFirst();
    }

    public int updateDraft(SiteConfigKey key, JsonNode draft, int expectedLockVersion, Long actorId) {
        return jdbc.update("""
                        UPDATE cms_config
                           SET draft_json = ?, draft_version = draft_version + 1,
                               lock_version = lock_version + 1, updated_at = ?, updated_by = ?
                         WHERE config_key = ? AND lock_version = ? AND deleted = 0
                        """,
                objectMapper.writeValueAsString(draft), UtcTime.now(), actorId,
                key.name(), expectedLockVersion);
    }

    public int publish(SiteConfigKey key, int expectedLockVersion, int expectedDraftVersion, Long actorId) {
        LocalDateTime now = UtcTime.now();
        return jdbc.update("""
                        UPDATE cms_config
                           SET published_json = draft_json,
                               publication_status = 'PUBLISHED',
                               published_version = draft_version,
                               published_at = ?,
                               published_by = ?,
                               lock_version = lock_version + 1,
                               updated_at = ?
                         WHERE config_key = ? AND lock_version = ? AND draft_version = ? AND deleted = 0
                        """,
                now, actorId, now, key.name(), expectedLockVersion, expectedDraftVersion);
    }
}

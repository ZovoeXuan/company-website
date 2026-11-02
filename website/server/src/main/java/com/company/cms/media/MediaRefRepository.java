package com.company.cms.media;

import com.company.cms.common.UtcTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 媒体引用表：记录某个草稿/发布快照在哪个字段用了哪张图。
 * 保存草稿与发布时按 DTO 重新计算，不做增量维护。
 */
@Repository
public class MediaRefRepository {

    public record RefEntry(String sourceKind, long sourceId, String stage, String fieldPath) {
    }

    private final JdbcTemplate jdbc;

    public MediaRefRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void replaceStage(String sourceKind, long sourceId, String stage,
                             List<MediaRefRow> refs) {
        jdbc.update("DELETE FROM cms_media_ref WHERE source_kind = ? AND source_id = ? AND stage = ?",
                sourceKind, sourceId, stage);
        if (refs.isEmpty()) {
            return;
        }
        jdbc.batchUpdate("""
                        INSERT INTO cms_media_ref
                            (source_kind, source_id, stage, media_id, field_path, created_at)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """,
                refs.stream()
                        .map(ref -> new Object[]{sourceKind, sourceId, stage, ref.mediaId(),
                                ref.fieldPath(), UtcTime.now()})
                        .toList());
    }

    public void copyStage(String sourceKind, long sourceId, String fromStage, String toStage) {
        jdbc.update("DELETE FROM cms_media_ref WHERE source_kind = ? AND source_id = ? AND stage = ?",
                sourceKind, sourceId, toStage);
        jdbc.update("""
                        INSERT INTO cms_media_ref
                            (source_kind, source_id, stage, media_id, field_path, created_at)
                        SELECT source_kind, source_id, ?, media_id, field_path, ?
                          FROM cms_media_ref
                         WHERE source_kind = ? AND source_id = ? AND stage = ?
                        """,
                toStage, UtcTime.now(), sourceKind, sourceId, fromStage);
    }

    public void deleteBySource(String sourceKind, long sourceId) {
        jdbc.update("DELETE FROM cms_media_ref WHERE source_kind = ? AND source_id = ?",
                sourceKind, sourceId);
    }

    public List<RefEntry> findByMedia(long mediaId) {
        return jdbc.query("""
                        SELECT source_kind, source_id, stage, field_path
                          FROM cms_media_ref WHERE media_id = ?
                         ORDER BY stage, source_kind, source_id
                        """,
                (rs, rowNum) -> new RefEntry(rs.getString("source_kind"), rs.getLong("source_id"),
                        rs.getString("stage"), rs.getString("field_path")),
                mediaId);
    }

    public boolean existsByMedia(long mediaId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM cms_media_ref WHERE media_id = ?", Long.class, mediaId);
        return count != null && count > 0;
    }

    public record MediaRefRow(long mediaId, String fieldPath) {
    }
}

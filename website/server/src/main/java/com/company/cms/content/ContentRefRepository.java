package com.company.cms.content;

import com.company.cms.common.UtcTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 内容关联表：产品关联案例、方案关联产品等，目标必须是同库内容。
 */
@Repository
public class ContentRefRepository {

    public record RefRow(long targetContentId, String fieldPath) {
    }

    public record IncomingRef(long sourceId, String sourceKind, String stage, String fieldPath) {
    }

    private final JdbcTemplate jdbc;

    public ContentRefRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void replaceStage(String sourceKind, long sourceId, String stage, List<RefRow> refs) {
        jdbc.update("DELETE FROM cms_content_ref WHERE source_kind = ? AND source_id = ? AND stage = ?",
                sourceKind, sourceId, stage);
        if (refs.isEmpty()) {
            return;
        }
        jdbc.batchUpdate("""
                        INSERT INTO cms_content_ref
                            (source_kind, source_id, stage, target_content_id, field_path, created_at)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """,
                refs.stream()
                        .map(ref -> new Object[]{sourceKind, sourceId, stage, ref.targetContentId(),
                                ref.fieldPath(), UtcTime.now()})
                        .toList());
    }

    public void copyStage(String sourceKind, long sourceId, String fromStage, String toStage) {
        jdbc.update("DELETE FROM cms_content_ref WHERE source_kind = ? AND source_id = ? AND stage = ?",
                sourceKind, sourceId, toStage);
        jdbc.update("""
                        INSERT INTO cms_content_ref
                            (source_kind, source_id, stage, target_content_id, field_path, created_at)
                        SELECT source_kind, source_id, ?, target_content_id, field_path, ?
                          FROM cms_content_ref
                         WHERE source_kind = ? AND source_id = ? AND stage = ?
                        """,
                toStage, UtcTime.now(), sourceKind, sourceId, fromStage);
    }

    public void deleteBySource(String sourceKind, long sourceId) {
        jdbc.update("DELETE FROM cms_content_ref WHERE source_kind = ? AND source_id = ?",
                sourceKind, sourceId);
    }

    /** 谁引用了这个内容（用于删除前检查与引用面板）。 */
    public List<IncomingRef> findIncoming(long targetContentId) {
        return jdbc.query("""
                        SELECT source_kind, source_id, stage, field_path
                          FROM cms_content_ref
                         WHERE target_content_id = ?
                         ORDER BY stage, source_kind, source_id
                        """,
                (rs, rowNum) -> new IncomingRef(rs.getLong("source_id"), rs.getString("source_kind"),
                        rs.getString("stage"), rs.getString("field_path")),
                targetContentId);
    }

    public boolean hasIncoming(long targetContentId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM cms_content_ref WHERE target_content_id = ?",
                Long.class, targetContentId);
        return count != null && count > 0;
    }

    /** 发布快照引用的目标内容，用于详情页的关联展示。 */
    public List<Long> findPublishedTargets(String sourceKind, long sourceId) {
        return jdbc.queryForList("""
                SELECT target_content_id FROM cms_content_ref
                 WHERE source_kind = ? AND source_id = ? AND stage = 'PUBLISHED'
                 ORDER BY id
                """, Long.class, sourceKind, sourceId);
    }
}

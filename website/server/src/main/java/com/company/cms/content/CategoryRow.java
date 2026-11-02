package com.company.cms.content;

import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

/**
 * cms_category 行。published_name 与 published_sort_order 只允许发布事务写入。
 */
public record CategoryRow(
        long id,
        ContentType categoryType,
        JsonNode draft,
        JsonNode published,
        PublicationStatus status,
        int draftVersion,
        Integer publishedVersion,
        int lockVersion,
        String publishedName,
        Integer publishedSortOrder,
        boolean deleted
) {
    public String draftName() {
        JsonNode name = draft == null ? null : draft.get("name");
        return name == null ? null : name.asString();
    }

    public int draftSortOrder() {
        JsonNode sort = draft == null ? null : draft.get("sortOrder");
        return sort == null || !sort.canConvertToInt() ? 9999 : sort.asInt();
    }

    public boolean hasUnpublishedChanges() {
        return status != PublicationStatus.PUBLISHED || publishedVersion == null
                || draftVersion > publishedVersion;
    }
}

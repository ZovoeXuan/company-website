package com.company.cms.content;

import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

/**
 * cms_content 行。草稿与发布快照分开保存，互不影响。
 */
public record ContentRow(
        long id,
        ContentType contentType,
        JsonNode draft,
        JsonNode published,
        PublicationStatus status,
        int draftVersion,
        Integer publishedVersion,
        int lockVersion,
        String publishedTitle,
        Long publishedCategoryId,
        Integer publishedSortOrder,
        LocalDateTime publishedDisplayTime,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt,
        boolean deleted
) {
    public boolean hasUnpublishedChanges() {
        return status != PublicationStatus.PUBLISHED || publishedVersion == null
                || draftVersion > publishedVersion;
    }
}

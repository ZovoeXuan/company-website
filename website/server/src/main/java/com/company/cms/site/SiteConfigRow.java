package com.company.cms.site;

import com.company.cms.content.PublicationStatus;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record SiteConfigRow(
        long id,
        SiteConfigKey key,
        JsonNode draft,
        JsonNode published,
        PublicationStatus status,
        int draftVersion,
        Integer publishedVersion,
        int lockVersion,
        LocalDateTime publishedAt
) {
    public boolean hasUnpublishedChanges() {
        return status != PublicationStatus.PUBLISHED || publishedVersion == null
                || draftVersion > publishedVersion;
    }
}

package com.company.cms.media;

import java.time.LocalDateTime;

public record MediaRow(
        long id,
        String storageKey,
        String originalName,
        String extension,
        String mimeType,
        long byteSize,
        int width,
        int height,
        LocalDateTime createdAt
) {
}

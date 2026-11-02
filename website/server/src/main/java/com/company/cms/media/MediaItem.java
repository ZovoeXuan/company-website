package com.company.cms.media;

/**
 * 素材列表项。url 为公开访问地址，仅在内容发布后可用。
 */
public record MediaItem(
        String id,
        String originalName,
        String mimeType,
        int width,
        int height,
        long byteSize,
        String url,
        String createdAt,
        boolean referenced
) {
}

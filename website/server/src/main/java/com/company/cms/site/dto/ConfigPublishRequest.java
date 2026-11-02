package com.company.cms.site.dto;

import jakarta.validation.constraints.NotNull;

public record ConfigPublishRequest(
        @NotNull(message = "缺少版本号") Integer expectedLockVersion,
        @NotNull(message = "缺少草稿版本号") Integer expectedDraftVersion
) {
}

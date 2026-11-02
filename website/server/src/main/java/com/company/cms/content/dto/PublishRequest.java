package com.company.cms.content.dto;

import jakarta.validation.constraints.NotNull;

public record PublishRequest(
        @NotNull(message = "缺少版本号") Integer expectedLockVersion,
        @NotNull(message = "缺少草稿版本号") Integer expectedDraftVersion
) {
}

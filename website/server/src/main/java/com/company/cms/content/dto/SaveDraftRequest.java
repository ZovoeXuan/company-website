package com.company.cms.content.dto;

import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

public record SaveDraftRequest(
        @NotNull(message = "缺少版本号") Integer expectedLockVersion,
        @NotNull(message = "缺少草稿内容") JsonNode draft
) {
}

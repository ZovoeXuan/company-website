package com.company.cms.site.dto;

import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

public record ConfigDraftRequest(
        @NotNull(message = "缺少版本号") Integer expectedLockVersion,
        @NotNull(message = "缺少配置内容") JsonNode draft
) {
}

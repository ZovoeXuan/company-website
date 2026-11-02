package com.company.cms.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

public record CreateContentRequest(
        @NotBlank(message = "请指定内容类型") String contentType,
        @NotNull(message = "缺少草稿内容") JsonNode draft
) {
}

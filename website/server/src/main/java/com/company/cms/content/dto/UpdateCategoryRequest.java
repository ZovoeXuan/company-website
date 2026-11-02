package com.company.cms.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 修改分类草稿。类型不可修改，故请求体不含 categoryType。
 */
public record UpdateCategoryRequest(
        @NotNull(message = "缺少版本号") Integer expectedLockVersion,
        @NotBlank(message = "请填写分类名称")
        @Size(max = 30, message = "分类名称最多 30 个字符") String name,
        Integer sortOrder
) {
}

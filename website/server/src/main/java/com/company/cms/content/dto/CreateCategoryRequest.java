package com.company.cms.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新建分类。名称长度与数据库 CHECK（1~30）一致。
 */
public record CreateCategoryRequest(
        @NotBlank(message = "请指定分类类型") String categoryType,
        @NotBlank(message = "请填写分类名称")
        @Size(max = 30, message = "分类名称最多 30 个字符") String name,
        Integer sortOrder
) {
}

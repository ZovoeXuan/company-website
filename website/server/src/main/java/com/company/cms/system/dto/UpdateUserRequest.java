package com.company.cms.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotNull(message = "缺少版本号")
        Integer expectedLockVersion,

        @NotBlank(message = "请输入显示名称")
        @Size(max = 50, message = "显示名称最多 50 个字符")
        String displayName,

        @NotBlank(message = "请选择角色")
        @Pattern(regexp = "^(ADMIN|EDITOR)$", message = "角色只能是 ADMIN 或 EDITOR")
        String roleCode,

        @NotNull(message = "缺少启用状态")
        Boolean enabled
) {
}

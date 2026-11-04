package com.company.cms.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 新建账号。账号规则见文档 4.2：4~50 位英文字母、数字、下划线、连字符。
 */
public record CreateUserRequest(
        @NotBlank(message = "请输入账号")
        @Pattern(regexp = "^[A-Za-z0-9_-]{4,50}$", message = "账号只能包含字母、数字、下划线、连字符，长度 4 到 50")
        String username,

        @NotBlank(message = "请输入显示名称")
        @Size(max = 50, message = "显示名称最多 50 个字符")
        String displayName,

        @NotBlank(message = "请选择角色")
        @Pattern(regexp = "^(ADMIN|EDITOR)$", message = "角色只能是 ADMIN 或 EDITOR")
        String roleCode,

        @NotBlank(message = "请输入初始密码")
        @Size(min = 6, max = 128, message = "密码长度需在 6 到 128 位之间")
        String initialPassword
) {
}

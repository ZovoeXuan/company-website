package com.company.cms.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改本人密码。密码不 trim，长度按文档 4.2：至少 12 位，最长 128。
 */
public record ChangePasswordRequest(
        @NotBlank(message = "请输入原密码")
        String oldPassword,

        @NotBlank(message = "请输入新密码")
        @Size(min = 12, max = 128, message = "新密码长度需在 12 到 128 位之间")
        String newPassword
) {
}

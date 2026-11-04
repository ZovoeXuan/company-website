package com.company.cms.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotNull(message = "缺少版本号")
        Integer expectedLockVersion,

        @NotBlank(message = "请输入新密码")
        @Size(min = 6, max = 128, message = "密码长度需在 6 到 128 位之间")
        String newPassword
) {
}

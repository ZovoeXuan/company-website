package com.company.cms.system;

import java.time.LocalDateTime;

/**
 * sys_user 行。密码哈希只在此层流转，不进入任何响应 DTO。
 */
public record SysUser(
        long id,
        String username,
        String passwordHash,
        String displayName,
        RoleCode roleCode,
        boolean enabled,
        int lockVersion,
        LocalDateTime lastLoginAt
) {
}

package com.company.cms.system.dto;

/**
 * 账号列表项。不含密码哈希。
 */
public record UserListItem(
        String id,
        String username,
        String displayName,
        String role,
        boolean enabled,
        int lockVersion,
        String lastLoginAt
) {
}

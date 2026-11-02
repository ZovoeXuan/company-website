package com.company.cms.system;

import com.company.cms.common.ApiException;

import java.util.Arrays;

/**
 * 固定两个角色，数据库 CHECK 只允许 ADMIN / EDITOR。
 */
public enum RoleCode {
    ADMIN,
    EDITOR;

    public static RoleCode parse(String raw) {
        return Arrays.stream(values())
                .filter(role -> role.name().equals(raw))
                .findFirst()
                .orElseThrow(() -> ApiException.validation("roleCode", "角色只能是 ADMIN 或 EDITOR"));
    }
}

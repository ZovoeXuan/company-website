package com.company.cms.auth;

/**
 * 当前登录人信息。不含密码哈希、锁定版本等内部字段。
 */
public record MeResponse(String id, String username, String displayName, String role) {

    public static MeResponse of(CmsPrincipal principal) {
        return new MeResponse(String.valueOf(principal.id()), principal.username(),
                principal.displayName(), principal.roleCode().name());
    }
}

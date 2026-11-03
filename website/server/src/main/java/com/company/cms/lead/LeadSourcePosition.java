package com.company.cms.lead;

import com.company.cms.common.ApiException;

import java.util.Arrays;

/**
 * 咨询入口位置白名单，与数据库 CHECK 约束一致。隐藏字段可被篡改，服务端必须校验。
 */
public enum LeadSourcePosition {
    HOME_HERO,
    HOME_BOTTOM,
    PRODUCT_DETAIL,
    SOLUTION_DETAIL,
    CASE_DETAIL,
    CONTACT,
    FLOATING,
    NAVIGATION,
    FOOTER,
    OTHER_CTA;

    public static LeadSourcePosition parse(String raw) {
        String value = raw == null ? "" : raw.trim();
        return Arrays.stream(values())
                .filter(position -> position.name().equals(value))
                .findFirst()
                .orElseThrow(() -> ApiException.validation("sourcePosition", "咨询入口位置不合法"));
    }
}

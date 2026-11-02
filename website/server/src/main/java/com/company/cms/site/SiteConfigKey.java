package com.company.cms.site;

import com.company.cms.common.ApiException;

import java.util.Arrays;

/**
 * 六个固定配置键，与数据库 CHECK 约束一致，不可增删。
 */
public enum SiteConfigKey {
    SITE,
    NAVIGATION,
    HOME,
    ABOUT,
    CONTACT,
    PRIVACY;

    public static SiteConfigKey parse(String raw) {
        return Arrays.stream(values())
                .filter(key -> key.name().equals(raw))
                .findFirst()
                .orElseThrow(() -> ApiException.validation("configKey", "配置键不合法"));
    }
}

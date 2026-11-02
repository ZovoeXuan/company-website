package com.company.cms.content;

import com.company.cms.common.ApiException;

import java.util.Arrays;

/**
 * 固定内容类型，与数据库 CHECK 约束一致。
 */
public enum ContentType {
    PRODUCT,
    SOLUTION,
    CASE,
    NEWS,
    CUSTOMER;

    public static ContentType parse(String raw) {
        return Arrays.stream(values())
                .filter(type -> type.name().equals(raw))
                .findFirst()
                .orElseThrow(() -> ApiException.validation("contentType", "内容类型不合法"));
    }

    /** 客户标识不分类，其余类型必须归属一个分类才能发布。 */
    public boolean requiresCategoryOnPublish() {
        return this != CUSTOMER;
    }

    public boolean requiresCategory() {
        return this != CUSTOMER;
    }
}

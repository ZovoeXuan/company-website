package com.company.cms.common;

import java.util.List;

/**
 * 统一分页结构，见开发文档 9.2。
 */
public record PageResult<T>(List<T> items, int page, int pageSize, long total) {

    public static <T> PageResult<T> of(List<T> items, int page, int pageSize, long total) {
        return new PageResult<>(items, page, pageSize, total);
    }
}

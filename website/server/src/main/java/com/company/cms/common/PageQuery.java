package com.company.cms.common;

/**
 * 分页参数校验：page 从 1 开始，pageSize 最大 50。
 */
public record PageQuery(int page, int pageSize) {

    public static PageQuery of(Integer page, Integer pageSize, int defaultSize) {
        int p = page == null ? 1 : page;
        int s = pageSize == null ? defaultSize : pageSize;
        if (p < 1) {
            throw ApiException.validation("page", "页码从 1 开始");
        }
        if (s < 1 || s > 50) {
            throw ApiException.validation("pageSize", "每页数量需在 1 到 50 之间");
        }
        return new PageQuery(p, s);
    }

    public int offset() {
        return (page - 1) * pageSize;
    }
}

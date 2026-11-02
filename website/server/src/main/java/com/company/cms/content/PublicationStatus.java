package com.company.cms.content;

/**
 * 发布状态。OFFLINE 表示曾经发布、当前下线，快照仍保留。
 */
public enum PublicationStatus {
    UNPUBLISHED,
    PUBLISHED,
    OFFLINE;

    public boolean isPublished() {
        return this == PUBLISHED;
    }
}

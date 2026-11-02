package com.company.cms.dashboard;

import java.util.Map;

/**
 * 工作台统计。EDITOR 看不到账号数与待处理咨询，字段为 null。
 */
public record DashboardResponse(
        long publishedContents,
        long draftContents,
        Map<String, Long> contentsByType,
        Long pendingLeads,
        Long userCount
) {
}

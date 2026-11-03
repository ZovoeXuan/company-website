package com.company.cms.lead;

/**
 * 官网咨询提交载荷。字段约束由 LeadService 统一校验，避免隐藏字段与客户端校验不一致。
 */
public record LeadSubmitRequest(
        String name,
        String phone,
        String companyName,
        String demand,
        String sourcePath,
        String sourcePosition,
        String contentType,
        Long contentId,
        Boolean consent,
        Integer consentVersion
) {
}

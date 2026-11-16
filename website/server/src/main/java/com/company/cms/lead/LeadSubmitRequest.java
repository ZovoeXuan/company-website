package com.company.cms.lead;

/**
 * 官网咨询提交载荷。包含可选 smsCode（当后台开启 smsVerifyEnabled 时由 LeadService 强制校验）。
 */
public record LeadSubmitRequest(
        String name,
        String phone,
        String smsCode,
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

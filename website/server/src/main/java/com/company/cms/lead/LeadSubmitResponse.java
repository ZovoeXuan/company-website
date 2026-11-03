package com.company.cms.lead;

/**
 * 成功受理只返回回执号，不回显手机号，也不提供查询其他线索的接口。
 */
public record LeadSubmitResponse(boolean accepted, String receiptNo) {
}

package com.company.cms.common;

import java.util.List;

/**
 * 字段级错误，与开发文档 9.1 的 fieldErrors 结构一致。
 */
public record FieldErrorItem(String field, String message) {
}

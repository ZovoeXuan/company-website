package com.company.cms.lead;

import com.company.cms.common.ApiException;

import java.util.Arrays;

/**
 * 咨询线索状态，与数据库 CHECK 约束一致。
 */
public enum LeadStatus {
    NEW,
    CONTACTED,
    CLOSED,
    INVALID;

    public static LeadStatus parse(String raw) {
        String value = raw == null ? "" : raw.trim();
        return Arrays.stream(values())
                .filter(status -> status.name().equals(value))
                .findFirst()
                .orElseThrow(() -> ApiException.validation("status", "线索状态不合法"));
    }

    /** 状态流转规则：NEW → CONTACTED/INVALID；CONTACTED → CLOSED/INVALID；CLOSED/INVALID 只能带原因回到 CONTACTED。 */
    public boolean canMoveTo(LeadStatus target) {
        return switch (this) {
            case NEW -> target == CONTACTED || target == INVALID;
            case CONTACTED -> target == CLOSED || target == INVALID;
            case CLOSED, INVALID -> target == CONTACTED;
        };
    }

    /** 关闭或作废后重新跟进必须填写原因。 */
    public boolean requiresNoteToReopen() {
        return this == CLOSED || this == INVALID;
    }
}

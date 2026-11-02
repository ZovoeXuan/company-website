package com.company.cms.common;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 数据库中的 DATETIME(3) 不保存时区，应用统一写入 UTC。
 */
public final class UtcTime {

    private UtcTime() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}

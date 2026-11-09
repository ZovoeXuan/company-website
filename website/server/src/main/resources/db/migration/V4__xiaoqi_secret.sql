-- 小鑫 AI 接入配置：密钥独立存储，不进入站点配置 JSON，不随发布流程公开
CREATE TABLE xiaoqi_secret (
    id TINYINT UNSIGNED NOT NULL,
    api_url VARCHAR(500) NOT NULL DEFAULT '',
    api_key_cipher VARBINARY(1024) NULL COMMENT 'AES-256-GCM 信封',
    api_key_last4 VARCHAR(4) NOT NULL DEFAULT '',
    auth_header VARCHAR(64) NOT NULL DEFAULT 'Authorization',
    auth_prefix VARCHAR(32) NOT NULL DEFAULT 'Bearer ',
    model_name VARCHAR(100) NOT NULL DEFAULT '',
    system_prompt VARCHAR(4000) NOT NULL DEFAULT '',
    timeout_ms INT UNSIGNED NOT NULL DEFAULT 15000,
    updated_by BIGINT UNSIGNED NULL,
    updated_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_xiaoqi_single CHECK (id = 1),
    CONSTRAINT ck_xiaoqi_timeout CHECK (timeout_ms BETWEEN 1000 AND 60000),
    CONSTRAINT fk_xiaoqi_updated_by FOREIGN KEY (updated_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='小鑫 AI 接入配置（单行）';

INSERT INTO xiaoqi_secret (id, updated_at) VALUES (1, UTC_TIMESTAMP(3));

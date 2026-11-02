-- 公司官网 CMS 业务表基线，目标 MySQL 8.4 / InnoDB / utf8mb4。
-- 在已创建且已明确选中的空测试数据库执行；本脚本不创建/选择/删除数据库。
-- 仅建九张业务表，并初始化六条未发布的空配置草稿；不创建任何登录账号。
-- Spring Session JDBC 表须从项目实际依赖携带的 MySQL schema 单独迁移。
-- DATETIME 不携带时区：应用/JDBC 会话统一 UTC，应用必须填写 created_at/updated_at。
-- 不使用 IF NOT EXISTS 或 INSERT IGNORE，避免掩盖迁移冲突。
-- 本交付仅经过静态核对，尚未在真实 MySQL 8.4 实例执行；详见 schema-notes.md。

CREATE TABLE sys_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '对外 ID 序列化为字符串',
    username VARCHAR(50) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL,
    password_hash VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    display_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    enabled TINYINT UNSIGNED NOT NULL DEFAULT 1,
    lock_version INT UNSIGNED NOT NULL DEFAULT 0,
    last_login_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username),
    KEY idx_user_role_enabled (role_code, enabled, id),
    CONSTRAINT ck_user_role CHECK (role_code IN ('ADMIN', 'EDITOR')),
    CONSTRAINT ck_user_enabled CHECK (enabled IN (0, 1)),
    CONSTRAINT ck_user_username_length CHECK (CHAR_LENGTH(username) BETWEEN 4 AND 50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='后台账号；只禁用，不提供物理删除或默认生产密码';

CREATE TABLE cms_config (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    config_key VARCHAR(30) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    draft_json JSON NOT NULL,
    published_json JSON NULL,
    publication_status VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'UNPUBLISHED',
    draft_version INT UNSIGNED NOT NULL DEFAULT 1,
    published_version INT UNSIGNED NULL,
    lock_version INT UNSIGNED NOT NULL DEFAULT 0,
    published_at DATETIME(3) NULL,
    published_by BIGINT UNSIGNED NULL,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    created_by BIGINT UNSIGNED NULL,
    updated_by BIGINT UNSIGNED NULL,
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key),
    KEY idx_config_published_by (published_by),
    KEY idx_config_created_by (created_by),
    KEY idx_config_updated_by (updated_by),
    CONSTRAINT fk_config_published_by FOREIGN KEY (published_by) REFERENCES sys_user (id),
    CONSTRAINT fk_config_created_by FOREIGN KEY (created_by) REFERENCES sys_user (id),
    CONSTRAINT fk_config_updated_by FOREIGN KEY (updated_by) REFERENCES sys_user (id),
    CONSTRAINT ck_config_key CHECK (config_key IN ('SITE', 'NAVIGATION', 'HOME', 'ABOUT', 'CONTACT', 'PRIVACY')),
    CONSTRAINT ck_config_status CHECK (publication_status IN ('UNPUBLISHED', 'PUBLISHED')),
    CONSTRAINT ck_config_not_deleted CHECK (deleted = 0),
    CONSTRAINT ck_config_draft_object CHECK (JSON_TYPE(draft_json) = 'OBJECT'),
    CONSTRAINT ck_config_published_object CHECK (published_json IS NULL OR JSON_TYPE(published_json) = 'OBJECT'),
    CONSTRAINT ck_config_versions CHECK (draft_version >= 1 AND (published_version IS NULL OR (published_version >= 1 AND published_version <= draft_version))),
    CONSTRAINT ck_config_snapshot_state CHECK (
        (publication_status = 'UNPUBLISHED' AND published_json IS NULL AND published_version IS NULL AND published_at IS NULL)
        OR (publication_status = 'PUBLISHED' AND published_json IS NOT NULL AND published_version IS NOT NULL AND published_at IS NOT NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='六个固定配置单例；不可删除或整体下线';

CREATE TABLE cms_category (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    category_type VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    draft_json JSON NOT NULL,
    published_json JSON NULL,
    publication_status VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'UNPUBLISHED',
    draft_version INT UNSIGNED NOT NULL DEFAULT 1,
    published_version INT UNSIGNED NULL,
    lock_version INT UNSIGNED NOT NULL DEFAULT 0,
    published_name VARCHAR(30) NULL COMMENT '仅发布事务从快照提取',
    published_sort_order INT UNSIGNED NULL,
    active_published_name VARCHAR(30) GENERATED ALWAYS AS (
        CASE WHEN publication_status = 'PUBLISHED' AND deleted = 0 THEN published_name ELSE NULL END
    ) STORED COMMENT '并发发布同类型重名分类的唯一键，应用禁止赋值',
    published_at DATETIME(3) NULL,
    published_by BIGINT UNSIGNED NULL,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    created_by BIGINT UNSIGNED NULL,
    updated_by BIGINT UNSIGNED NULL,
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_active_name (category_type, active_published_name),
    KEY idx_category_public (category_type, publication_status, deleted, published_sort_order, id),
    KEY idx_category_published_by (published_by),
    KEY idx_category_created_by (created_by),
    KEY idx_category_updated_by (updated_by),
    CONSTRAINT fk_category_published_by FOREIGN KEY (published_by) REFERENCES sys_user (id),
    CONSTRAINT fk_category_created_by FOREIGN KEY (created_by) REFERENCES sys_user (id),
    CONSTRAINT fk_category_updated_by FOREIGN KEY (updated_by) REFERENCES sys_user (id),
    CONSTRAINT ck_category_type CHECK (category_type IN ('PRODUCT', 'SOLUTION', 'CASE', 'NEWS')),
    CONSTRAINT ck_category_status CHECK (publication_status IN ('UNPUBLISHED', 'PUBLISHED', 'OFFLINE')),
    CONSTRAINT ck_category_deleted CHECK (deleted IN (0, 1) AND (deleted = 0 OR publication_status <> 'PUBLISHED')),
    CONSTRAINT ck_category_draft_object CHECK (JSON_TYPE(draft_json) = 'OBJECT'),
    CONSTRAINT ck_category_published_object CHECK (published_json IS NULL OR JSON_TYPE(published_json) = 'OBJECT'),
    CONSTRAINT ck_category_versions CHECK (draft_version >= 1 AND (published_version IS NULL OR (published_version >= 1 AND published_version <= draft_version))),
    CONSTRAINT ck_category_snapshot_state CHECK (
        (publication_status = 'UNPUBLISHED' AND published_json IS NULL AND published_version IS NULL AND published_at IS NULL)
        OR (publication_status IN ('PUBLISHED', 'OFFLINE') AND published_json IS NOT NULL AND published_version IS NOT NULL AND published_at IS NOT NULL)
    ),
    CONSTRAINT ck_category_projections CHECK (
        (published_json IS NULL AND published_name IS NULL AND published_sort_order IS NULL)
        OR (published_json IS NOT NULL AND published_name IS NOT NULL AND CHAR_LENGTH(published_name) BETWEEN 1 AND 30 AND published_sort_order IS NOT NULL AND published_sort_order <= 9999)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='单层分类；草稿和发布快照分开；CUSTOMER 不分类';

CREATE TABLE cms_content (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    content_type VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    draft_json JSON NOT NULL,
    published_json JSON NULL,
    publication_status VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'UNPUBLISHED',
    draft_version INT UNSIGNED NOT NULL DEFAULT 1,
    published_version INT UNSIGNED NULL,
    lock_version INT UNSIGNED NOT NULL DEFAULT 0,
    published_title VARCHAR(120) NULL COMMENT '只能在发布事务中由快照提取',
    published_category_id BIGINT UNSIGNED NULL,
    published_sort_order INT UNSIGNED NULL,
    published_display_time DATETIME(3) NULL COMMENT 'NEWS 的业务展示时间，对应 DTO displayTime',
    published_at DATETIME(3) NULL,
    published_by BIGINT UNSIGNED NULL,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    created_by BIGINT UNSIGNED NULL,
    updated_by BIGINT UNSIGNED NULL,
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_content_public_sort (content_type, publication_status, deleted, published_sort_order, id),
    KEY idx_content_public_category (content_type, publication_status, deleted, published_category_id, published_sort_order, id),
    KEY idx_content_public_news (content_type, publication_status, deleted, published_display_time DESC, id DESC),
    KEY idx_content_public_news_category (content_type, publication_status, deleted, published_category_id, published_display_time DESC, id DESC),
    KEY idx_content_published_category (published_category_id),
    KEY idx_content_published_by (published_by),
    KEY idx_content_created_by (created_by),
    KEY idx_content_updated_by (updated_by),
    CONSTRAINT fk_content_category FOREIGN KEY (published_category_id) REFERENCES cms_category (id),
    CONSTRAINT fk_content_published_by FOREIGN KEY (published_by) REFERENCES sys_user (id),
    CONSTRAINT fk_content_created_by FOREIGN KEY (created_by) REFERENCES sys_user (id),
    CONSTRAINT fk_content_updated_by FOREIGN KEY (updated_by) REFERENCES sys_user (id),
    CONSTRAINT ck_content_type CHECK (content_type IN ('PRODUCT', 'SOLUTION', 'CASE', 'NEWS', 'CUSTOMER')),
    CONSTRAINT ck_content_status CHECK (publication_status IN ('UNPUBLISHED', 'PUBLISHED', 'OFFLINE')),
    CONSTRAINT ck_content_deleted CHECK (deleted IN (0, 1) AND (deleted = 0 OR publication_status <> 'PUBLISHED')),
    CONSTRAINT ck_content_draft_object CHECK (JSON_TYPE(draft_json) = 'OBJECT'),
    CONSTRAINT ck_content_published_object CHECK (published_json IS NULL OR JSON_TYPE(published_json) = 'OBJECT'),
    CONSTRAINT ck_content_versions CHECK (draft_version >= 1 AND (published_version IS NULL OR (published_version >= 1 AND published_version <= draft_version))),
    CONSTRAINT ck_content_snapshot_state CHECK (
        (publication_status = 'UNPUBLISHED' AND published_json IS NULL AND published_version IS NULL AND published_at IS NULL)
        OR (publication_status IN ('PUBLISHED', 'OFFLINE') AND published_json IS NOT NULL AND published_version IS NOT NULL AND published_at IS NOT NULL)
    ),
    CONSTRAINT ck_content_projections CHECK (
        (published_json IS NULL AND published_title IS NULL AND published_sort_order IS NULL AND published_display_time IS NULL)
        OR (published_json IS NOT NULL AND published_title IS NOT NULL AND CHAR_LENGTH(published_title) BETWEEN 1 AND 120 AND published_sort_order IS NOT NULL AND published_sort_order <= 9999)
    ),
    CONSTRAINT ck_content_display_time CHECK (
        (content_type = 'NEWS' AND (published_json IS NULL OR published_display_time IS NOT NULL))
        OR (content_type <> 'NEWS' AND published_display_time IS NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='固定类型内容；稳定 ID 路由，不使用 slug';

CREATE TABLE cms_media (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    storage_key VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '服务端随机存储键，不是客户端路径',
    original_name VARCHAR(255) NOT NULL,
    extension VARCHAR(8) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    mime_type VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    byte_size BIGINT UNSIGNED NOT NULL,
    width INT UNSIGNED NOT NULL,
    height INT UNSIGNED NOT NULL,
    uploaded_by BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(3) NOT NULL,
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_media_storage_key (storage_key),
    KEY idx_media_created (deleted, created_at DESC, id DESC),
    KEY idx_media_uploader (uploaded_by),
    CONSTRAINT fk_media_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES sys_user (id),
    CONSTRAINT ck_media_extension CHECK (extension IN ('jpg', 'jpeg', 'png', 'webp')),
    CONSTRAINT ck_media_mime CHECK (mime_type IN ('image/jpeg', 'image/png', 'image/webp')),
    CONSTRAINT ck_media_size CHECK (byte_size BETWEEN 1 AND 5242880),
    CONSTRAINT ck_media_pixels CHECK (width >= 1 AND height >= 1 AND CAST(width AS DECIMAL(20,0)) * height <= 20000000),
    CONSTRAINT ck_media_deleted CHECK (deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='首版仅图片；服务端验证并落盘成功后才写入记录';

CREATE TABLE cms_media_ref (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    source_kind VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    source_id BIGINT UNSIGNED NOT NULL,
    stage VARCHAR(10) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    media_id BIGINT UNSIGNED NOT NULL,
    field_path VARCHAR(512) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    created_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_media_ref_position (source_kind, source_id, stage, media_id, field_path),
    KEY idx_media_ref_target (media_id, stage, source_kind, source_id),
    CONSTRAINT fk_media_ref_target FOREIGN KEY (media_id) REFERENCES cms_media (id),
    CONSTRAINT ck_media_ref_source_kind CHECK (source_kind IN ('CONFIG', 'CONTENT', 'CATEGORY')),
    CONSTRAINT ck_media_ref_source_id CHECK (source_id > 0),
    CONSTRAINT ck_media_ref_stage CHECK (stage IN ('DRAFT', 'PUBLISHED')),
    CONSTRAINT ck_media_ref_path CHECK (CHAR_LENGTH(field_path) BETWEEN 1 AND 512)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='多态来源由应用校验；含正文图片；公开访问还需校验来源当前有效';

CREATE TABLE cms_content_ref (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    source_kind VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    source_id BIGINT UNSIGNED NOT NULL,
    stage VARCHAR(10) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    target_content_id BIGINT UNSIGNED NOT NULL,
    field_path VARCHAR(512) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    created_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_content_ref_position (source_kind, source_id, stage, target_content_id, field_path),
    KEY idx_content_ref_target (target_content_id, stage, source_kind, source_id),
    CONSTRAINT fk_content_ref_target FOREIGN KEY (target_content_id) REFERENCES cms_content (id),
    CONSTRAINT ck_content_ref_source_kind CHECK (source_kind IN ('CONFIG', 'CONTENT', 'CATEGORY')),
    CONSTRAINT ck_content_ref_source_id CHECK (source_id > 0),
    CONSTRAINT ck_content_ref_stage CHECK (stage IN ('DRAFT', 'PUBLISHED')),
    CONSTRAINT ck_content_ref_path CHECK (CHAR_LENGTH(field_path) BETWEEN 1 AND 512)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='内容关联，不存分类关联；保存/发布时从 DTO 重新计算';

CREATE TABLE cms_lead (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    receipt_no VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '服务端安全随机回执，不暴露顺序 ID',
    idempotency_key CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '规范化小写 UUID，全局唯一',
    payload_hash BINARY(32) NOT NULL COMMENT '规范化请求的 HMAC-SHA256 摘要，原始 32 字节',
    payload_hash_version SMALLINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '对应规范化规则和摘要密钥版本',
    name VARCHAR(50) NOT NULL,
    company_name VARCHAR(100) NOT NULL,
    phone_ciphertext VARBINARY(1024) NOT NULL COMMENT '认证加密信封，含算法/密钥版本、nonce、密文、tag',
    phone_last4 CHAR(4) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    demand VARCHAR(2000) NOT NULL,
    source_path VARCHAR(200) NOT NULL,
    source_position VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    content_type VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NULL,
    content_id BIGINT UNSIGNED NULL,
    consent_version INT UNSIGNED NOT NULL COMMENT 'PRIVACY 的 published_version；对外可返回数字字符串',
    consent_at DATETIME(3) NOT NULL,
    status VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'NEW',
    last_note VARCHAR(2000) NULL,
    handled_by BIGINT UNSIGNED NULL,
    handled_at DATETIME(3) NULL,
    lock_version INT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_lead_receipt (receipt_no),
    UNIQUE KEY uk_lead_idempotency (idempotency_key),
    KEY idx_lead_created (created_at DESC, id DESC),
    KEY idx_lead_status_created (status, created_at DESC, id DESC),
    KEY idx_lead_content (content_id),
    KEY idx_lead_handler (handled_by),
    CONSTRAINT fk_lead_content FOREIGN KEY (content_id) REFERENCES cms_content (id),
    CONSTRAINT fk_lead_handler FOREIGN KEY (handled_by) REFERENCES sys_user (id),
    CONSTRAINT ck_lead_payload_version CHECK (payload_hash_version >= 1),
    CONSTRAINT ck_lead_name CHECK (CHAR_LENGTH(name) BETWEEN 1 AND 50),
    CONSTRAINT ck_lead_company CHECK (CHAR_LENGTH(company_name) BETWEEN 1 AND 100),
    CONSTRAINT ck_lead_demand CHECK (CHAR_LENGTH(demand) BETWEEN 1 AND 2000),
    CONSTRAINT ck_lead_phone_last4 CHECK (CHAR_LENGTH(phone_last4) = 4),
    CONSTRAINT ck_lead_source_position CHECK (source_position IN ('HOME_HERO', 'HOME_BOTTOM', 'PRODUCT_DETAIL', 'SOLUTION_DETAIL', 'CASE_DETAIL', 'CONTACT', 'FLOATING', 'NAVIGATION', 'FOOTER', 'OTHER_CTA')),
    CONSTRAINT ck_lead_content_type CHECK (content_type IS NULL OR content_type IN ('PRODUCT', 'SOLUTION', 'CASE')),
    CONSTRAINT ck_lead_consent_version CHECK (consent_version >= 1),
    CONSTRAINT ck_lead_status CHECK (status IN ('NEW', 'CONTACTED', 'CLOSED', 'INVALID'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='咨询线索与幂等结果；无首版删除/导出接口，不保存原始 IP 或明文手机号';

CREATE TABLE sys_audit_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    actor_id BIGINT UNSIGNED NULL COMMENT '匿名登录失败等场景允许空',
    action VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    target_type VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NULL,
    target_id BIGINT UNSIGNED NULL,
    draft_version INT UNSIGNED NULL,
    published_version INT UNSIGNED NULL,
    request_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    result VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    summary VARCHAR(500) NOT NULL COMMENT '仅必要概况，不存密码/token/手机号/需求或备注全文',
    created_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_audit_created (created_at DESC, id DESC),
    KEY idx_audit_actor_created (actor_id, created_at DESC, id DESC),
    KEY idx_audit_target (target_type, target_id, created_at DESC, id DESC),
    KEY idx_audit_request (request_id),
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES sys_user (id),
    CONSTRAINT ck_audit_result CHECK (result IN ('SUCCESS', 'FAILURE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='后台只读审计；成功业务操作与日志同事务提交';

-- 六条空配置均无发布快照。{} 只代表合法空草稿，不代表能通过发布校验。
INSERT INTO cms_config
    (config_key, draft_json, created_at, updated_at)
VALUES
    ('SITE', JSON_OBJECT(), UTC_TIMESTAMP(3), UTC_TIMESTAMP(3)),
    ('NAVIGATION', JSON_OBJECT(), UTC_TIMESTAMP(3), UTC_TIMESTAMP(3)),
    ('HOME', JSON_OBJECT(), UTC_TIMESTAMP(3), UTC_TIMESTAMP(3)),
    ('ABOUT', JSON_OBJECT(), UTC_TIMESTAMP(3), UTC_TIMESTAMP(3)),
    ('CONTACT', JSON_OBJECT(), UTC_TIMESTAMP(3), UTC_TIMESTAMP(3)),
    ('PRIVACY', JSON_OBJECT(), UTC_TIMESTAMP(3), UTC_TIMESTAMP(3));

-- 至此业务基线结束。不存在默认账号、生产内容、触发器、存储过程、定时事件或破坏性语句。

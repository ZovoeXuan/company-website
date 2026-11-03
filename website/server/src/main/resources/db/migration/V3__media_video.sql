-- 阶段 7：素材库支持视频（首屏、主图与后台媒体管理）
ALTER TABLE cms_media
    DROP CHECK ck_media_extension,
    DROP CHECK ck_media_mime,
    DROP CHECK ck_media_size,
    DROP CHECK ck_media_pixels;

ALTER TABLE cms_media
    ADD COLUMN media_type VARCHAR(8) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'IMAGE'
        COMMENT 'IMAGE/VIDEO' AFTER mime_type,
    ADD COLUMN duration_ms INT UNSIGNED NULL COMMENT '视频时长（毫秒），图片为空' AFTER height,
    ADD CONSTRAINT ck_media_type CHECK (media_type IN ('IMAGE', 'VIDEO')),
    ADD CONSTRAINT ck_media_extension CHECK (extension IN ('jpg', 'jpeg', 'png', 'webp', 'mp4', 'webm')),
    ADD CONSTRAINT ck_media_mime CHECK (mime_type IN ('image/jpeg', 'image/png', 'image/webp', 'video/mp4', 'video/webm')),
    ADD CONSTRAINT ck_media_size CHECK (byte_size BETWEEN 1 AND 52428800),
    ADD CONSTRAINT ck_media_pixels CHECK (
        (width = 0 AND height = 0)
        OR (width >= 1 AND height >= 1 AND CAST(width AS DECIMAL(20, 0)) * height <= 20000000)
    );

ALTER TABLE cms_media COMMENT='图片与视频素材；服务端验证并落盘成功后才写入记录';

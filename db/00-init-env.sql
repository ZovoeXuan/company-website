-- ============================================================
-- 本地开发环境初始化：数据库 + 账号（一次性执行，用 root 运行）
-- 目标版本：MySQL 8.0.46（本项目实测版本）/ 8.4 LTS 同样适用
-- 注意：本文件只建库和账号，不建业务表；业务表由 Flyway V1 迁移创建。
-- 警告：请勿把真实密码写回本文件并提交；执行后自行妥善保存。
-- ============================================================

-- 1. 三个环境库（开发 / 测试；生产库在服务器上单独建）
CREATE DATABASE IF NOT EXISTS cms_dev
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

CREATE DATABASE IF NOT EXISTS cms_test
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

-- 2. 迁移账号：只用于执行 Flyway 建表/改表，不给业务运行使用
CREATE USER IF NOT EXISTS 'cms_migrator'@'localhost'
  IDENTIFIED BY '__CHANGE_ME_MIGRATOR__';
GRANT ALL PRIVILEGES ON cms_dev.*  TO 'cms_migrator'@'localhost';
GRANT ALL PRIVILEGES ON cms_test.* TO 'cms_migrator'@'localhost';

-- 3. 应用账号：只读写数据，不允许改结构
CREATE USER IF NOT EXISTS 'cms_app'@'localhost'
  IDENTIFIED BY '__CHANGE_ME_APP__';
GRANT SELECT, INSERT, UPDATE, DELETE ON cms_dev.*  TO 'cms_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON cms_test.* TO 'cms_app'@'localhost';

FLUSH PRIVILEGES;

-- 4. 自检：确认库与字符集
SELECT SCHEMA_NAME, DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME IN ('cms_dev', 'cms_test');

-- 提示：应用连接串需带 connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true，
-- 且 useSSL=false 时补充 allowPublicKeyRetrieval=true（MySQL 8 默认 caching_sha2_password）。

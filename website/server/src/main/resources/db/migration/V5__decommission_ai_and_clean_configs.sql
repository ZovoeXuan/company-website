-- V5: 彻底下线 AI 助手密钥表，并清理站点配置中的 xiaoqiConfig 历史字段
DROP TABLE IF EXISTS xiaoqi_secret;

UPDATE cms_config
   SET draft_json = JSON_REMOVE(draft_json, '$.xiaoqiConfig'),
       published_json = CASE
           WHEN published_json IS NOT NULL THEN JSON_REMOVE(published_json, '$.xiaoqiConfig')
           ELSE NULL
       END
 WHERE config_key = 'SITE';

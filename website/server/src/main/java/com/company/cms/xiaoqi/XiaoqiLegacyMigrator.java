package com.company.cms.xiaoqi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * 旧版把 apiUrl/apiKey 存在 SITE 配置 JSON 的 xiaoqiConfig 里。启动时若发现，
 * 转存到加密的 xiaoqi_secret（仅当其尚未配置），再从草稿与发布版 JSON 中删除。
 */
@Component
public class XiaoqiLegacyMigrator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(XiaoqiLegacyMigrator.class);
    private static final List<String> LEGACY = List.of("apiUrl", "apiKey", "modelName", "systemPrompt");

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final XiaoqiSecretRepository repository;

    public XiaoqiLegacyMigrator(JdbcTemplate jdbc, ObjectMapper objectMapper, XiaoqiSecretRepository repository) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT draft_json, published_json FROM cms_config WHERE config_key = 'SITE'");
        if (rows.isEmpty()) return;
        JsonNode draft = read(rows.get(0).get("draft_json"));
        JsonNode published = read(rows.get(0).get("published_json"));
        JsonNode source = hasLegacy(published) ? published.get("xiaoqiConfig")
                : hasLegacy(draft) ? draft.get("xiaoqiConfig") : null;
        if (source == null) return;

        XiaoqiSecret current = repository.load();
        String url = source.path("apiUrl").asString("").trim();
        if (!current.configured() && url.startsWith("http")) {
            String key = source.path("apiKey").asString("").trim();
            repository.save(new XiaoqiSecret(url, key, XiaoqiSecretRepository.last4(key),
                    current.authHeader(), current.authPrefix(),
                    source.path("modelName").asString("").trim(),
                    source.path("systemPrompt").asString("").trim(), current.timeoutMs()), null);
            log.info("已将旧版小鑫接入配置迁移到 xiaoqi_secret");
        }
        String paths = "'$.xiaoqiConfig.apiUrl', '$.xiaoqiConfig.apiKey', "
                + "'$.xiaoqiConfig.modelName', '$.xiaoqiConfig.systemPrompt'";
        jdbc.update("UPDATE cms_config SET draft_json = JSON_REMOVE(draft_json, " + paths + "), "
                + "published_json = IF(published_json IS NULL, NULL, JSON_REMOVE(published_json, " + paths + ")) "
                + "WHERE config_key = 'SITE'");
        log.info("已从 SITE 配置中移除旧版小鑫密钥字段");
    }

    private boolean hasLegacy(JsonNode site) {
        if (site == null) return false;
        JsonNode xq = site.get("xiaoqiConfig");
        return xq != null && xq.isObject() && LEGACY.stream().anyMatch(xq::has);
    }

    private JsonNode read(Object raw) {
        return raw == null ? null : objectMapper.readTree(raw.toString());
    }
}

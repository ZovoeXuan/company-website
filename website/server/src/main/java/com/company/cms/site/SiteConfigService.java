package com.company.cms.site;

import com.company.cms.auth.CmsPrincipal;
import com.company.cms.common.ApiException;
import com.company.cms.common.ErrorCode;
import com.company.cms.system.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 站点配置：草稿保存、发布、公开读取。六个键互不影响，各自独立发布。
 */
@Service
public class SiteConfigService {

    private final SiteConfigRepository repository;
    private final SiteConfigValidator validator;
    private final AuditLogService audit;

    public SiteConfigService(SiteConfigRepository repository, SiteConfigValidator validator,
                             AuditLogService audit) {
        this.repository = repository;
        this.validator = validator;
        this.audit = audit;
    }

    public Map<String, Object> adminView(SiteConfigKey key) {
        SiteConfigRow row = repository.find(key)
                .orElseThrow(() -> ApiException.notFound("配置不存在"));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("key", key.name());
        data.put("draft", row.draft());
        data.put("published", row.published());
        data.put("status", row.status().name());
        data.put("draftVersion", row.draftVersion());
        data.put("publishedVersion", row.publishedVersion());
        data.put("lockVersion", row.lockVersion());
        data.put("hasUnpublishedChanges", row.hasUnpublishedChanges());
        data.put("publishedAt", row.publishedAt() == null ? null
                : row.publishedAt().atOffset(ZoneOffset.UTC).toString());
        return data;
    }

    @Transactional
    public Map<String, Object> saveDraft(SiteConfigKey key, JsonNode draft, int expectedLockVersion,
                                         CmsPrincipal actor) {
        JsonNode normalized = validator.validate(key, draft);
        int updated = repository.updateDraft(key, normalized, expectedLockVersion, actor.id());
        if (updated == 0) {
            throw new ApiException(ErrorCode.VERSION_CONFLICT, "配置已被他人修改，请刷新后重试");
        }
        return adminView(key);
    }

    @Transactional
    public void publish(SiteConfigKey key, int expectedLockVersion, int expectedDraftVersion,
                        CmsPrincipal actor) {
        SiteConfigRow row = repository.find(key)
                .orElseThrow(() -> ApiException.notFound("配置不存在"));
        if (row.draftVersion() != expectedDraftVersion) {
            throw new ApiException(ErrorCode.VERSION_CONFLICT, "草稿已变化，请刷新后重新发布");
        }
        // 发布前再校验一次，避免历史草稿绕过新增规则
        validator.validate(key, row.draft());

        int updated = repository.publish(key, expectedLockVersion, expectedDraftVersion, actor.id());
        if (updated == 0) {
            throw new ApiException(ErrorCode.VERSION_CONFLICT, "配置已被他人修改，请刷新后重试");
        }
        audit.success(actor.id(), "CONFIG_PUBLISH", "CONFIG", null, "发布配置 " + key.name());
    }

    /**
     * 公开站点信息：SITE 未发布时整体 404，前端据此提示「站点尚未初始化」。
     */
    public Map<String, Object> publicSiteConfig() {
        SiteConfigRow site = requirePublished(SiteConfigKey.SITE);
        JsonNode siteJson = site.published();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("siteName", text(siteJson, "siteName"));
        data.put("slogan", text(siteJson, "slogan"));

        JsonNode logoId = siteJson.get("logoMediaId");
        data.put("logoUrl", logoId == null || !logoId.isNumber()
                ? null : "/api/public/media/" + logoId.asLong());

        data.put("nav", visibleLinks(publishedOrEmpty(SiteConfigKey.NAVIGATION), "items"));

        Map<String, Object> contact = new LinkedHashMap<>();
        JsonNode contactJson = siteJson.get("contact");
        contact.put("phone", text(contactJson, "phone"));
        contact.put("email", text(contactJson, "email"));
        contact.put("address", text(contactJson, "address"));
        contact.put("hours", text(contactJson, "hours"));
        data.put("contact", contact);

        Map<String, Object> footer = new LinkedHashMap<>();
        footer.put("copyright", text(siteJson, "copyright"));
        footer.put("icp", text(siteJson, "icp"));
        footer.put("links", visibleLinks(publishedOrEmpty(SiteConfigKey.NAVIGATION), "footerLinks"));
        data.put("footer", footer);

        // CONTACT 未发布时按「表单未启用」处理，不应让站点信息整体失败
        JsonNode contactPage = publishedOrEmpty(SiteConfigKey.CONTACT);
        data.put("formEnabled", contactPage != null
                && contactPage.get("formEnabled") != null
                && contactPage.get("formEnabled").asBoolean(false));

        SiteConfigRow privacy = repository.find(SiteConfigKey.PRIVACY).orElse(null);
        data.put("privacyVersion", privacy != null && privacy.publishedVersion() != null
                ? privacy.publishedVersion() : null);
        return data;
    }

    public Map<String, Object> publicPage(String code) {
        SiteConfigKey key;
        try {
            key = SiteConfigKey.parse(code);
        } catch (RuntimeException ex) {
            throw ApiException.notFound("页面不存在");
        }
        if (key == SiteConfigKey.SITE || key == SiteConfigKey.NAVIGATION || key == SiteConfigKey.HOME) {
            throw ApiException.notFound("页面不存在");
        }
        SiteConfigRow row = requirePublished(key);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", key.name());
        data.put("content", row.published());
        return data;
    }

    private SiteConfigRow requirePublished(SiteConfigKey key) {
        SiteConfigRow row = repository.find(key).orElseThrow(() -> ApiException.notFound("配置不存在"));
        if (!row.status().isPublished() || row.published() == null) {
            throw ApiException.notFound("内容尚未发布");
        }
        return row;
    }

    private JsonNode publishedOrEmpty(SiteConfigKey key) {
        return repository.find(key).map(SiteConfigRow::published).orElse(null);
    }

    private List<Map<String, Object>> visibleLinks(JsonNode json, String field) {
        List<Map<String, Object>> links = new ArrayList<>();
        if (json == null) {
            return links;
        }
        JsonNode items = json.get(field);
        if (items == null || !items.isArray()) {
            return links;
        }
        for (JsonNode item : items) {
            if (item.get("visible") != null && !item.get("visible").asBoolean(true)) {
                continue;
            }
            Map<String, Object> link = new LinkedHashMap<>();
            link.put("label", text(item, "label"));
            link.put("path", text(item, "path"));
            links.add(link);
        }
        return links;
    }

    private String text(JsonNode node, String field) {
        if (node == null) {
            return "";
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asString();
    }
}

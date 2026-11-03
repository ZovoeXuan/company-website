package com.company.cms.site;

import com.company.cms.auth.CmsPrincipal;
import com.company.cms.common.ApiException;
import com.company.cms.common.ErrorCode;
import com.company.cms.common.FieldErrorItem;
import com.company.cms.content.JsonRefs;
import com.company.cms.media.MediaRefRepository;
import com.company.cms.system.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 站点配置：草稿保存、发布、公开读取。六个键互不影响，各自独立发布。
 */
@Service
public class SiteConfigService {

    private final SiteConfigRepository repository;
    private final SiteConfigValidator validator;
    private final LinkResolver links;
    private final AuditLogService audit;
    /** 配置里的图片也要登记引用，否则公开图片接口无法判断它是否可见 */
    private final MediaRefRepository mediaRefs;

    public SiteConfigService(SiteConfigRepository repository, SiteConfigValidator validator,
                             LinkResolver links, AuditLogService audit,
                             MediaRefRepository mediaRefs) {
        this.repository = repository;
        this.validator = validator;
        this.links = links;
        this.audit = audit;
        this.mediaRefs = mediaRefs;
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
        SiteConfigRow saved = repository.find(key)
                .orElseThrow(() -> ApiException.notFound("配置不存在"));
        replaceDraftRefs(saved.id(), normalized);
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
        // 发布前再校验一次结构与必填，避免历史草稿绕过新增规则
        JsonNode normalized = validator.validate(key, row.draft());
        validator.validateForPublish(key, normalized);
        if (key == SiteConfigKey.CONTACT) {
            checkFormPrerequisites(normalized);
        }

        int updated = repository.publish(key, expectedLockVersion, expectedDraftVersion, actor.id());
        if (updated == 0) {
            throw new ApiException(ErrorCode.VERSION_CONFLICT, "配置已被他人修改，请刷新后重试");
        }
        mediaRefs.copyStage("CONFIG", row.id(), "DRAFT", "PUBLISHED");
        audit.success(actor.id(), "CONFIG_PUBLISH", "CONFIG", null, "发布配置 " + key.name());
    }

    /**
     * 开启咨询表单前必须先有可展示的隐私说明与联系方式（文档 6.8）。
     * 放在发布阶段拦，避免线上出现「表单能提交但没有隐私依据」的状态。
     */
    private void checkFormPrerequisites(JsonNode contact) {
        JsonNode enabled = contact.get("formEnabled");
        if (enabled == null || !enabled.asBoolean(false)) {
            return;
        }
        List<FieldErrorItem> errors = new ArrayList<>();
        SiteConfigRow privacy = repository.find(SiteConfigKey.PRIVACY).orElse(null);
        if (privacy == null || privacy.publishedVersion() == null || !privacy.status().isPublished()) {
            errors.add(new FieldErrorItem("formEnabled", "开启表单前，隐私说明必须先发布"));
        }
        String phone = text(contact, "phone");
        String email = text(contact, "email");
        if (phone.isBlank() && email.isBlank()) {
            errors.add(new FieldErrorItem("formEnabled", "开启表单前，请至少填写电话或邮箱"));
        }
        if (!errors.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "还不能开启咨询表单", errors);
        }
    }

    /** 配置内所有 MediaId 字段登记为草稿引用，发布时整体复制。 */
    private void replaceDraftRefs(long configId, JsonNode draft) {
        List<MediaRefRepository.MediaRefRow> rows = JsonRefs.mediaRefs(draft).stream()
                .map(ref -> new MediaRefRepository.MediaRefRow(ref.mediaId(), ref.path()))
                .toList();
        mediaRefs.replaceStage("CONFIG", configId, "DRAFT", rows);
    }

    /**
     * 公开站点信息：SITE 未发布时整体 404，前端据此提示「站点尚未初始化」。
     */
    public Map<String, Object> publicSiteConfig() {
        SiteConfigRow site = requirePublished(SiteConfigKey.SITE);
        JsonNode siteJson = site.published();
        JsonNode contactJson = publishedOrEmpty(SiteConfigKey.CONTACT);
        boolean formEnabled = contactJson != null && contactJson.get("formEnabled") != null
                && contactJson.get("formEnabled").asBoolean(false);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("siteName", text(siteJson, "siteName"));
        data.put("slogan", text(siteJson, "slogan"));

        String logoUrl = mediaUrl(siteJson.get("logoMediaId"));
        data.put("logoUrl", logoUrl);
        data.put("logoAlt", text(siteJson, "logoAlt").isBlank()
                ? text(siteJson, "siteName") : text(siteJson, "logoAlt"));
        data.put("faviconUrl", mediaUrl(siteJson.get("faviconMediaId")));

        JsonNode seo = siteJson.get("defaultSeo");
        Map<String, Object> seoView = new LinkedHashMap<>();
        seoView.put("title", text(seo, "title"));
        seoView.put("description", text(seo, "description"));
        data.put("defaultSeo", seoView);

        data.put("nav", nav(publishedOrEmpty(SiteConfigKey.NAVIGATION), formEnabled));

        // 联系方式只在 CONTACT 维护；旧快照里的 SITE.contact 作为兼容回退
        Map<String, Object> contact = new LinkedHashMap<>();
        contact.put("phone", firstNonBlank(text(contactJson, "phone"), text(siteJson.get("contact"), "phone")));
        contact.put("email", firstNonBlank(text(contactJson, "email"), text(siteJson.get("contact"), "email")));
        contact.put("address", firstNonBlank(text(contactJson, "address"), text(siteJson.get("contact"), "address")));
        contact.put("hours", firstNonBlank(text(contactJson, "hours"), text(siteJson.get("contact"), "hours")));
        data.put("contact", contact);

        Map<String, Object> footer = new LinkedHashMap<>();
        footer.put("copyright", firstNonBlank(text(siteJson, "copyrightText"), text(siteJson, "copyright")));
        footer.put("icp", firstNonBlank(text(siteJson, "icpText"), text(siteJson, "icp")));
        footer.put("icpUrl", text(siteJson, "icpUrl"));
        footer.put("policeText", text(siteJson, "policeText"));
        footer.put("policeUrl", text(siteJson, "policeUrl"));
        List<Map<String, Object>> groups = footerGroups(siteJson, formEnabled);
        footer.put("groups", groups);
        // 兼容旧前端：拍平后的链接列表
        List<Map<String, Object>> flat = new ArrayList<>();
        for (Map<String, Object> group : groups) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> groupLinks = (List<Map<String, Object>>) group.get("links");
            flat.addAll(groupLinks);
        }
        footer.put("links", flat);
        data.put("footer", footer);

        data.put("formEnabled", formEnabled);

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

    /** 导航：解析有效链接，叶子目标失效则隐藏，父节点无有效子项也隐藏。 */
    private List<Map<String, Object>> nav(JsonNode json, boolean formEnabled) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (json == null) {
            return result;
        }
        JsonNode items = json.get("items");
        if (items == null || !items.isArray()) {
            return result;
        }
        for (JsonNode item : items) {
            if (!isEnabled(item)) {
                continue;
            }
            Map<String, Object> entry = navEntry(item, formEnabled);
            if (entry == null) {
                continue;
            }
            JsonNode children = item.get("children");
            List<Map<String, Object>> childViews = new ArrayList<>();
            if (children != null && children.isArray()) {
                for (JsonNode child : children) {
                    if (!isEnabled(child)) {
                        continue;
                    }
                    Map<String, Object> childView = navEntry(child, formEnabled);
                    if (childView != null) {
                        childViews.add(childView);
                    }
                }
            }
            entry.put("children", childViews);
            // 既没有有效链接也没有有效子项的父节点不输出
            if ("".equals(entry.get("path")) && childViews.isEmpty()) {
                continue;
            }
            result.add(entry);
        }
        return result;
    }

    private Map<String, Object> navEntry(JsonNode item, boolean formEnabled) {
        Map<String, Object> action = links.resolve(item.get("link"), text(item, "path"), formEnabled, Set.of());
        String path = action == null ? null : (String) action.get("url");
        if (path != null && !path.startsWith("/")) {
            // 外链导航用 url 字段承载，path 留空避免前端当作站内路由
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("label", text(item, "label"));
            entry.put("path", "");
            entry.put("url", path);
            entry.put("newWindow", Boolean.TRUE.equals(action.get("newWindow")));
            entry.put("linkType", action.get("type"));
            return entry;
        }
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("label", text(item, "label"));
        entry.put("path", path == null ? "" : path);
        entry.put("linkType", action == null ? null : action.get("type"));
        return entry;
    }

    private List<Map<String, Object>> footerGroups(JsonNode siteJson, boolean formEnabled) {
        List<Map<String, Object>> groups = new ArrayList<>();
        JsonNode source = siteJson.get("footerGroups");
        if (source != null && source.isArray()) {
            for (JsonNode group : source) {
                List<Map<String, Object>> groupLinks = new ArrayList<>();
                JsonNode linksJson = group.get("links");
                if (linksJson != null && linksJson.isArray()) {
                    for (JsonNode item : linksJson) {
                        if (!isEnabled(item)) {
                            continue;
                        }
                        Map<String, Object> action = links.resolve(item.get("link"), null, formEnabled, Set.of());
                        if (action == null) {
                            continue;
                        }
                        Map<String, Object> link = new LinkedHashMap<>();
                        link.put("label", text(item, "label"));
                        Object url = action.get("url");
                        if (url != null && url.toString().startsWith("/")) {
                            link.put("path", url);
                            link.put("url", null);
                        } else {
                            link.put("path", "");
                            link.put("url", url);
                        }
                        link.put("newWindow", Boolean.TRUE.equals(action.get("newWindow")));
                        link.put("linkType", action.get("type"));
                        groupLinks.add(link);
                    }
                }
                if (groupLinks.isEmpty()) {
                    continue;
                }
                Map<String, Object> view = new LinkedHashMap<>();
                view.put("title", text(group, "title"));
                view.put("links", groupLinks);
                groups.add(view);
            }
        }
        // 旧快照：NAVIGATION.footerLinks
        if (groups.isEmpty()) {
            JsonNode legacy = publishedOrEmpty(SiteConfigKey.NAVIGATION);
            JsonNode legacyLinks = legacy == null ? null : legacy.get("footerLinks");
            if (legacyLinks != null && legacyLinks.isArray()) {
                List<Map<String, Object>> groupLinks = new ArrayList<>();
                for (JsonNode item : legacyLinks) {
                    if (item.get("visible") != null && !item.get("visible").asBoolean(true)) {
                        continue;
                    }
                    Map<String, Object> link = new LinkedHashMap<>();
                    link.put("label", text(item, "label"));
                    String legacyPath = text(item, "path");
                    link.put("path", legacyPath.startsWith("/") ? legacyPath : "");
                    link.put("url", legacyPath.startsWith("/") ? null : legacyPath);
                    groupLinks.add(link);
                }
                if (!groupLinks.isEmpty()) {
                    Map<String, Object> view = new LinkedHashMap<>();
                    view.put("title", "快速入口");
                    view.put("links", groupLinks);
                    groups.add(view);
                }
            }
        }
        return groups;
    }

    private boolean isEnabled(JsonNode node) {
        return node != null && (node.get("enabled") == null || node.get("enabled").asBoolean(true));
    }

    private String mediaUrl(JsonNode idNode) {
        if (idNode == null || !idNode.isNumber()) {
            return null;
        }
        return "/api/public/media/" + idNode.asLong();
    }

    private String firstNonBlank(String first, String second) {
        return first == null || first.isBlank() ? (second == null ? "" : second) : first;
    }

    private String text(JsonNode node, String field) {
        if (node == null) {
            return "";
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asString().trim();
    }
}

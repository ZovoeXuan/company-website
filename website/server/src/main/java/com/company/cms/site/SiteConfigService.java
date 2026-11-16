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
 * 公开接口严格执行正向字段白名单，不下发任何 AI 密钥或未定义扩展字段。
 */
@Service
public class SiteConfigService {

    private final SiteConfigRepository repository;
    private final SiteConfigValidator validator;
    private final LinkResolver links;
    private final AuditLogService audit;
    private final MediaRefRepository mediaRefs;
    private final PublicVersionService versionService;

    public SiteConfigService(SiteConfigRepository repository, SiteConfigValidator validator,
                             LinkResolver links, AuditLogService audit,
                             MediaRefRepository mediaRefs, PublicVersionService versionService) {
        this.repository = repository;
        this.validator = validator;
        this.links = links;
        this.audit = audit;
        this.mediaRefs = mediaRefs;
        this.versionService = versionService;
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
        versionService.evictAfterCommitOrNow();
    }

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

    private void replaceDraftRefs(long configId, JsonNode draft) {
        List<MediaRefRepository.MediaRefRow> rows = JsonRefs.mediaRefs(draft).stream()
                .map(ref -> new MediaRefRepository.MediaRefRow(ref.mediaId(), ref.path()))
                .toList();
        mediaRefs.replaceStage("CONFIG", configId, "DRAFT", rows);
    }

    public Map<String, Object> publicSiteConfig() {
        SiteConfigRow site = requirePublished(SiteConfigKey.SITE);
        JsonNode siteJson = site.published();
        SiteConfigRow contactRow = repository.find(SiteConfigKey.CONTACT).orElse(null);
        JsonNode contactJson = (contactRow != null && contactRow.status().isPublished()) ? contactRow.published() : null;
        boolean formEnabled = contactJson != null && contactJson.get("formEnabled") != null
                && contactJson.get("formEnabled").asBoolean(false);
        boolean smsVerifyEnabled = contactJson == null || contactJson.get("smsVerifyEnabled") == null
                || contactJson.get("smsVerifyEnabled").asBoolean(true);

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

        JsonNode navJson = publishedOrEmpty(SiteConfigKey.NAVIGATION);
        data.put("nav", nav(navJson, formEnabled));
        data.put("megaColumns", megaColumns(navJson));
        data.put("megaCta", megaCta(navJson));

        Map<String, Object> contact = new LinkedHashMap<>();
        contact.put("companyName", text(contactJson, "companyName"));
        contact.put("formTitle", text(contactJson, "formTitle").isBlank() ? "预约产品展示" : text(contactJson, "formTitle"));
        contact.put("successText", text(contactJson, "successText"));
        contact.put("smsVerifyEnabled", smsVerifyEnabled);
        contact.put("phone", firstNonBlank(text(contactJson, "phone"), text(siteJson.get("contact"), "phone")));
        contact.put("email", firstNonBlank(text(contactJson, "email"), text(siteJson.get("contact"), "email")));
        contact.put("address", firstNonBlank(text(contactJson, "address"), text(siteJson.get("contact"), "address")));
        contact.put("hours", firstNonBlank(text(contactJson, "hours"), text(siteJson.get("contact"), "hours")));
        contact.put("mapUrl", text(contactJson, "mapUrl"));
        contact.put("qrImageUrl", mediaUrl(contactJson == null ? null : contactJson.get("qrMediaId")));
        contact.put("qrLabel", text(contactJson, "qrLabel"));
        data.put("contact", contact);

        Map<String, Object> footer = new LinkedHashMap<>();
        footer.put("copyright", firstNonBlank(text(siteJson, "copyrightText"), text(siteJson, "copyright")));
        footer.put("icp", firstNonBlank(text(siteJson, "icpText"), text(siteJson, "icp")));
        footer.put("icpUrl", text(siteJson, "icpUrl"));
        footer.put("policeText", text(siteJson, "policeText"));
        footer.put("policeUrl", text(siteJson, "policeUrl"));
        List<Map<String, Object>> groups = footerGroups(siteJson, formEnabled);
        footer.put("groups", groups);
        List<Map<String, Object>> flat = new ArrayList<>();
        for (Map<String, Object> group : groups) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> groupLinks = (List<Map<String, Object>>) group.get("links");
            flat.addAll(groupLinks);
        }
        footer.put("links", flat);
        footer.put("qrCodes", publicQrCodes(siteJson.get("qrCodes")));
        data.put("footer", footer);

        data.put("formEnabled", formEnabled);

        List<Map<String, String>> emailList = new ArrayList<>();
        JsonNode emails = siteJson.get("contactEmails");
        if (emails != null && emails.isArray()) {
            for (JsonNode em : emails) {
                String eAddr = text(em, "email");
                if (!eAddr.isBlank()) {
                    Map<String, String> item = new LinkedHashMap<>();
                    item.put("email", eAddr);
                    item.put("desc", text(em, "desc"));
                    emailList.add(item);
                }
            }
        }
        data.put("contactEmails", emailList);

        JsonNode pageHeroes = siteJson.get("pageHeroes");
        if (pageHeroes != null && pageHeroes.isObject()) {
            data.put("pageHeroes", pageHeroes);
        }

        SiteConfigRow privacy = repository.find(SiteConfigKey.PRIVACY).orElse(null);
        data.put("siteConfigVersion", versionService.currentVersion().siteConfigVersion());
        data.put("privacyVersion", privacy != null && privacy.publishedVersion() != null && privacy.status().isPublished()
                ? privacy.publishedVersion() : null);
        return data;
    }

    private List<Map<String, Object>> publicQrCodes(JsonNode rawQrs) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (rawQrs == null || !rawQrs.isArray()) {
            return list;
        }
        for (JsonNode q : rawQrs) {
            if (q == null || !q.isObject()) continue;
            String label = text(q, "label");
            String url = mediaUrl(q.get("mediaId"));
            if (url == null || url.isBlank()) {
                String rawUrl = text(q, "imageUrl");
                if (rawUrl.startsWith("/api/public/media/") || rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
                    url = rawUrl;
                }
            }
            if (url != null && !url.isBlank()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("label", label.isBlank() ? "官方二维码" : label);
                item.put("imageUrl", url);
                if (q.get("mediaId") != null && q.get("mediaId").isNumber()) {
                    item.put("mediaId", q.get("mediaId").asLong());
                }
                list.add(item);
            }
        }
        return list;
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
        return repository.find(key)
                .filter(row -> row.status().isPublished())
                .map(SiteConfigRow::published)
                .orElse(null);
    }

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
            if ("".equals(entry.get("path")) && entry.get("url") == null
                    && !"CONSULT".equals(entry.get("linkType")) && childViews.isEmpty()) {
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
        return groups;
    }

    private List<Map<String, Object>> megaColumns(JsonNode navJson) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (navJson == null) {
            return result;
        }
        JsonNode cols = navJson.get("megaColumns");
        if (cols == null || !cols.isArray()) {
            return result;
        }
        for (JsonNode col : cols) {
            Map<String, Object> colMap = new LinkedHashMap<>();
            colMap.put("title", text(col, "title"));
            List<Map<String, Object>> itemsList = new ArrayList<>();
            JsonNode items = col.get("items");
            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    Map<String, Object> itemMap = new LinkedHashMap<>();
                    itemMap.put("label", text(item, "label"));
                    itemMap.put("path", text(item, "path"));
                    itemMap.put("tag", text(item, "tag"));
                    itemMap.put("isVideo", bool(item, "isVideo", false));
                    itemMap.put("videoUrl", text(item, "videoUrl"));
                    itemMap.put("videoTitle", text(item, "videoTitle"));
                    itemsList.add(itemMap);
                }
            }
            colMap.put("items", itemsList);
            result.add(colMap);
        }
        return result;
    }

    private Map<String, Object> megaCta(JsonNode navJson) {
        if (navJson == null) {
            return null;
        }
        JsonNode cta = navJson.get("megaCta");
        if (cta == null || !cta.isObject()) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("text", text(cta, "text"));
        map.put("linkText", text(cta, "linkText"));
        return map;
    }

    private boolean bool(JsonNode node, String field, boolean fallback) {
        if (node == null) return fallback;
        JsonNode val = node.get(field);
        return val == null || val.isNull() ? fallback : val.asBoolean(fallback);
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

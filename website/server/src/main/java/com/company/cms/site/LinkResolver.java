package com.company.cms.site;

import com.company.cms.common.ApiException;
import com.company.cms.common.FieldErrorItem;
import com.company.cms.content.ContentRepository;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 统一动作对象 link 的校验与解析（文档 6.1）。
 *
 * 草稿只保存类型与输入字段，公开接口输出解析后的安全 URL/动作，
 * 官网不根据数据库内容自行拼接地址。
 */
@Component
public class LinkResolver {

    /** PAGE 白名单，键为后台保存值，值为官网固定路由。 */
    private static final Map<String, String> PAGES = Map.of(
            "HOME", "/",
            "PRODUCTS", "/products",
            "SOLUTIONS", "/solutions",
            "CASES", "/cases",
            "NEWS", "/news",
            "ABOUT", "/about",
            "CONTACT", "/contact",
            "PRIVACY", "/privacy"
    );

    /** 首页可跳转的锚点白名单。 */
    public static final Set<String> ANCHORS =
            Set.of("metrics", "services", "cases", "customers", "news", "consultation");

    private static final Set<String> CONTENT_TYPES =
            Set.of("PRODUCT", "SOLUTION", "CASE", "NEWS");

    private final ObjectMapper objectMapper;
    private final ContentRepository contents;

    public LinkResolver(ObjectMapper objectMapper, ContentRepository contents) {
        this.objectMapper = objectMapper;
        this.contents = contents;
    }

    /** 校验并归一化一个必填 link；返回 null 表示未填写（由调用方决定是否允许）。 */
    public ObjectNode normalize(JsonNode raw, String path, String label,
                                SeedErrors errors) {
        if (raw == null || raw.isNull() || (raw.isObject() && raw.isEmpty())) {
            return null;
        }
        if (!raw.isObject()) {
            errors.add(path, label + "格式不正确");
            return null;
        }
        String type = text(raw, "type").trim().toUpperCase();
        switch (type) {
            case "NONE":
                return plain("NONE");
            case "PAGE": {
                String pageCode = text(raw, "pageCode").trim().toUpperCase();
                if (!PAGES.containsKey(pageCode)) {
                    errors.add(path + ".pageCode", "页面不在允许范围内");
                    return null;
                }
                ObjectNode node = plain("PAGE");
                node.put("pageCode", pageCode);
                return node;
            }
            case "CONTENT": {
                String contentType = text(raw, "contentType").trim().toUpperCase();
                if (!CONTENT_TYPES.contains(contentType)) {
                    errors.add(path + ".contentType", "内容类型不合法");
                    return null;
                }
                Long contentId = longOrNull(raw.get("contentId"));
                if (contentId == null) {
                    errors.add(path + ".contentId", "请选择目标内容");
                    return null;
                }
                ObjectNode node = plain("CONTENT");
                node.put("contentType", contentType);
                node.put("contentId", contentId);
                return node;
            }
            case "EXTERNAL": {
                String url = text(raw, "url").trim();
                if (!isExternalUrl(url)) {
                    errors.add(path + ".url", "只允许以 http:// 或 https:// 开头的完整地址");
                    return null;
                }
                ObjectNode node = plain("EXTERNAL");
                node.put("url", url);
                node.put("newWindow", raw.get("newWindow") != null && raw.get("newWindow").asBoolean(false));
                return node;
            }
            case "CONSULT": {
                ObjectNode node = plain("CONSULT");
                String contentType = text(raw, "contentType").trim().toUpperCase();
                if (!contentType.isEmpty()) {
                    if (!Set.of("PRODUCT", "SOLUTION", "CASE").contains(contentType)) {
                        errors.add(path + ".contentType", "咨询上下文只支持产品、方案、案例");
                        return null;
                    }
                    Long contentId = longOrNull(raw.get("contentId"));
                    if (contentId == null) {
                        errors.add(path + ".contentId", "请选择咨询上下文内容");
                        return null;
                    }
                    node.put("contentType", contentType);
                    node.put("contentId", contentId);
                }
                return node;
            }
            case "ANCHOR": {
                String anchorCode = text(raw, "anchorCode").trim().toLowerCase();
                if (!ANCHORS.contains(anchorCode)) {
                    errors.add(path + ".anchorCode", "锚点不在允许范围内");
                    return null;
                }
                ObjectNode node = plain("ANCHOR");
                node.put("anchorCode", anchorCode);
                return node;
            }
            default:
                errors.add(path + ".type", "请选择链接类型");
                return null;
        }
    }

    /**
     * 解析为公开动作。返回 null 表示该动作已失效（目标下线、表单关闭、锚点无内容），
     * 调用方应过滤掉整条按钮/导航项，而不是输出死链接。
     */
    public Map<String, Object> resolve(JsonNode link, String fallbackPath,
                                       boolean formEnabled, Set<String> availableAnchors) {
        // 兼容早期只保存 path 的导航快照
        if (link == null && fallbackPath != null && !fallbackPath.isBlank()) {
            return pageAction(fallbackPath);
        }
        if (link == null || !link.isObject()) {
            return null;
        }
        String type = text(link, "type").toUpperCase();
        switch (type) {
            case "PAGE": {
                String url = PAGES.get(text(link, "pageCode"));
                return url == null ? null : pageAction(url);
            }
            case "CONTENT": {
                Long id = longOrNull(link.get("contentId"));
                String contentType = text(link, "contentType");
                if (id == null || contents.findPublishedById(id).isEmpty()) {
                    return null;
                }
                String base = switch (contentType) {
                    case "PRODUCT" -> "/products/";
                    case "SOLUTION" -> "/solutions/";
                    case "CASE" -> "/cases/";
                    case "NEWS" -> "/news/";
                    default -> null;
                };
                if (base == null) {
                    return null;
                }
                Map<String, Object> action = new LinkedHashMap<>();
                action.put("type", "CONTENT");
                action.put("contentType", contentType);
                action.put("contentId", String.valueOf(id));
                action.put("url", base + id);
                return action;
            }
            case "EXTERNAL": {
                String url = text(link, "url");
                if (!isExternalUrl(url)) {
                    return null;
                }
                Map<String, Object> action = new LinkedHashMap<>();
                action.put("type", "EXTERNAL");
                action.put("url", url);
                action.put("newWindow", link.get("newWindow") != null && link.get("newWindow").asBoolean(false));
                return action;
            }
            case "CONSULT": {
                if (!formEnabled) {
                    return null;
                }
                Map<String, Object> action = new LinkedHashMap<>();
                action.put("type", "CONSULT");
                Long id = longOrNull(link.get("contentId"));
                if (id != null) {
                    if (contents.findPublishedById(id).isEmpty()) {
                        return null;
                    }
                    action.put("contentType", text(link, "contentType"));
                    action.put("contentId", String.valueOf(id));
                }
                return action;
            }
            case "ANCHOR": {
                String anchorCode = text(link, "anchorCode");
                if (!availableAnchors.contains(anchorCode)) {
                    return null;
                }
                Map<String, Object> action = new LinkedHashMap<>();
                action.put("type", "ANCHOR");
                action.put("url", "/#" + anchorCode);
                return action;
            }
            default:
                return null;
        }
    }

    /** 发布校验：目标必须存在且已发布、类型匹配。 */
    public boolean publishable(JsonNode link, String expectedContentType) {
        if (link == null || !link.isObject()) {
            return false;
        }
        String type = text(link, "type").toUpperCase();
        if ("CONTENT".equals(type)) {
            Long id = longOrNull(link.get("contentId"));
            if (id == null) {
                return false;
            }
            String contentType = text(link, "contentType");
            if (expectedContentType != null && !expectedContentType.equals(contentType)) {
                return false;
            }
            return contents.findPublishedById(id)
                    .filter(row -> row.contentType().name().equals(contentType))
                    .isPresent();
        }
        if ("CONSULT".equals(type)) {
            Long id = longOrNull(link.get("contentId"));
            return id == null || contents.findPublishedById(id).isPresent();
        }
        return true;
    }

    /** 提取 link 引用的内容 ID，供引用表与删除保护使用。 */
    public Long referencedContentId(JsonNode link) {
        if (link == null || !link.isObject()) {
            return null;
        }
        String type = text(link, "type").toUpperCase();
        if (!"CONTENT".equals(type) && !"CONSULT".equals(type)) {
            return null;
        }
        return longOrNull(link.get("contentId"));
    }

    private ObjectNode plain(String type) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", type);
        return node;
    }

    private Map<String, Object> pageAction(String url) {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("type", "PAGE");
        action.put("url", url);
        return action;
    }

    private boolean isExternalUrl(String url) {
        if (url == null || url.isBlank() || url.length() > 500) {
            return false;
        }
        String lower = url.toLowerCase();
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            return false;
        }
        return !lower.startsWith("http://javascript") && !lower.contains("\n") && !lower.contains("\r");
    }

    private String text(JsonNode node, String field) {
        if (node == null) {
            return "";
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asString();
    }

    private Long longOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            long value = node.asLong();
            return value <= 0 ? null : value;
        }
        String raw = node.asString().trim();
        if (raw.isEmpty()) {
            return null;
        }
        try {
            long value = Long.parseLong(raw);
            return value <= 0 ? null : value;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /** 校验错误收集器，避免把 List 到处传递。 */
    public static final class SeedErrors {
        private final java.util.List<FieldErrorItem> items = new java.util.ArrayList<>();

        public void add(String field, String message) {
            items.add(new FieldErrorItem(field, message));
        }

        public boolean isEmpty() {
            return items.isEmpty();
        }

        public java.util.List<FieldErrorItem> items() {
            return items;
        }

        public void throwIfAny(String message) {
            if (!items.isEmpty()) {
                throw new ApiException(com.company.cms.common.ErrorCode.VALIDATION_ERROR, message, items);
            }
        }
    }
}

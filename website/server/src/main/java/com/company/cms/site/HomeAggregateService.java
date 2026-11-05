package com.company.cms.site;

import com.company.cms.common.ApiException;
import com.company.cms.content.ContentRepository;
import com.company.cms.content.ContentRow;
import com.company.cms.media.MediaRepository;
import com.company.cms.media.MediaRow;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 首页公开聚合（文档 6.4 / 10.4）。
 *
 * 所有关联内容与图片一次性批量读取，避免按卡片逐条查库；
 * 失效引用在输出阶段过滤，不能生成死链接。
 */
@Service
public class HomeAggregateService {

    private final SiteConfigRepository configs;
    private final ContentRepository contents;
    private final MediaRepository mediaRepository;
    private final LinkResolver links;

    public HomeAggregateService(SiteConfigRepository configs, ContentRepository contents,
                                MediaRepository mediaRepository, LinkResolver links) {
        this.configs = configs;
        this.contents = contents;
        this.mediaRepository = mediaRepository;
        this.links = links;
    }

    public Map<String, Object> publicHome() {
        SiteConfigRow row = configs.find(SiteConfigKey.HOME)
                .orElseThrow(() -> ApiException.notFound("首页尚未初始化"));
        if (!row.status().isPublished() || row.published() == null) {
            throw ApiException.notFound("首页尚未发布");
        }
        return build(row.published(), row.publishedVersion(), row.publishedAt());
    }

    /**
     * 草稿预览：首屏与各区块获取当前草稿，关联内容仍只取各自已发布版本（文档 5.4）。
     * 与公开接口共用同一段聚合逻辑，避免预览和线上出现两套结构。
     */
    public Map<String, Object> previewHome() {
        SiteConfigRow row = configs.find(SiteConfigKey.HOME)
                .orElseThrow(() -> ApiException.notFound("首页尚未初始化"));
        JsonNode draft = row.draft();
        if (draft == null || draft.isEmpty()) {
            throw ApiException.notFound("首页草稿尚未填写");
        }
        return build(draft, row.draftVersion(), row.publishedAt());
    }

    private Map<String, Object> build(JsonNode cfg, Integer version, LocalDateTime publishedAt) {
        boolean formEnabled = formEnabled();

        // ---- 第一批：关联内容（一次 IN 查询）
        Set<Long> contentIds = new LinkedHashSet<>();
        collectIds(cfg.get("services"), "groups", contentIds);
        collectIds(cfg.get("cases"), null, contentIds);
        collectIds(cfg.get("customers"), null, contentIds);
        collectIds(cfg.get("news"), null, contentIds);
        Map<Long, ContentRow> contentMap = new HashMap<>(contents.findPublishedByIds(contentIds).stream()
                .collect(Collectors.toMap(ContentRow::id, Function.identity())));
        JsonNode newsBlock = cfg.get("news");
        if (newsBlock != null && enabled(newsBlock) && "LATEST".equals(text(newsBlock, "mode"))) {
            int limit = newsBlock.get("limit") != null ? Math.max(1, Math.min(6, newsBlock.get("limit").asInt())) : 3;
            for (ContentRow r : contents.findLatestPublishedNews(limit)) {
                contentMap.put(r.id(), r);
            }
        }

        // ---- 第二批：媒体（首屏、分组配图、卡片封面）
        Set<Long> mediaIds = new LinkedHashSet<>();
        JsonNode heroItems = cfg.get("hero") == null ? null : cfg.get("hero").get("items");
        if (heroItems != null && heroItems.isArray()) {
            for (JsonNode item : heroItems) {
                addIfNumber(mediaIds, item.get("desktopMediaId"));
                addIfNumber(mediaIds, item.get("mobileMediaId"));
            }
        }
        JsonNode groups = cfg.get("services") == null ? null : cfg.get("services").get("groups");
        if (groups != null && groups.isArray()) {
            for (JsonNode group : groups) {
                addIfNumber(mediaIds, group.get("imageMediaId"));
            }
        }
        for (ContentRow content : contentMap.values()) {
            JsonNode json = content.published();
            if (json != null) {
                addIfNumber(mediaIds, json.get("coverMediaId"));
                addIfNumber(mediaIds, json.get("logoMediaId"));
            }
        }
        Map<Long, MediaRow> mediaMap = mediaRepository.findByIds(mediaIds).stream()
                .collect(Collectors.toMap(MediaRow::id, Function.identity()));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("version", version);
        data.put("publishedAt", publishedAt == null ? null
                : publishedAt.atOffset(ZoneOffset.UTC).toString());

        // ---- 各区块先组装，再据此判断锚点是否可用
        Map<String, Object> hero = hero(cfg.get("hero"), mediaMap);
        Map<String, Object> metrics = metrics(cfg.get("metrics"));
        Map<String, Object> services = services(cfg.get("services"), contentMap, mediaMap, formEnabled);
        Map<String, Object> cases = cases(cfg.get("cases"), contentMap, mediaMap);
        Map<String, Object> customers = customers(cfg.get("customers"), contentMap, mediaMap);
        Map<String, Object> news = news(cfg.get("news"), contentMap, mediaMap);

        Set<String> anchors = new HashSet<>();
        if (metrics != null) {
            anchors.add("metrics");
        }
        if (services != null) {
            anchors.add("services");
        }
        if (cases != null) {
            anchors.add("cases");
        }
        if (customers != null) {
            anchors.add("customers");
        }
        if (news != null) {
            anchors.add("news");
        }
        anchors.add("consultation");

        // 首屏与咨询动作依赖锚点集合，放在最后解析
        enrichHeroLinks(hero, formEnabled, anchors);
        List<String> order = new ArrayList<>();
        JsonNode orderNode = cfg.get("sectionOrder");
        if (orderNode != null && orderNode.isArray()) {
            for (JsonNode n : orderNode) {
                if (n != null && n.isString() && !order.contains(n.asString())) {
                    order.add(n.asString());
                }
            }
        }
        for (String defKey : List.of("metrics", "services", "cases", "customers", "news", "consultation")) {
            if (!order.contains(defKey)) {
                order.add(defKey);
            }
        }
        data.put("sectionOrder", order);
        data.put("hero", hero);
        data.put("metrics", metrics);
        data.put("services", services);
        data.put("cases", cases);
        data.put("customers", customers);
        data.put("news", news);
        data.put("consultation", consultation(cfg.get("consultation"), formEnabled, anchors));
        return data;
    }

    private void collectIds(JsonNode block, String arrayField, Set<Long> target) {
        if (block == null || !block.isObject()) {
            return;
        }
        if (arrayField == null) {
            addIds(block.get("contentIds"), target);
            return;
        }
        JsonNode list = block.get(arrayField);
        if (list != null && list.isArray()) {
            for (JsonNode item : list) {
                addIds(item.get("contentIds"), target);
            }
        }
    }

    private void addIds(JsonNode array, Set<Long> target) {
        if (array == null || !array.isArray()) {
            return;
        }
        for (JsonNode node : array) {
            if (node != null && node.isNumber()) {
                target.add(node.asLong());
            }
        }
    }

    private void addIfNumber(Set<Long> target, JsonNode node) {
        if (node != null && node.isNumber()) {
            target.add(node.asLong());
        }
    }

    // -------------------------------------------------- hero

    private Map<String, Object> hero(JsonNode block, Map<Long, MediaRow> mediaMap) {
        List<Map<String, Object>> items = new ArrayList<>();
        JsonNode list = block == null ? null : block.get("items");
        if (list != null && list.isArray()) {
            for (JsonNode item : list) {
                boolean enabled = item.get("enabled") == null || item.get("enabled").asBoolean(true);
                if (!enabled) {
                    continue;
                }
                String title = text(item, "title");
                Map<String, Object> desktop = media(item.get("desktopMediaId"), mediaMap,
                        text(item, "imageAlt").isBlank() ? title : text(item, "imageAlt"));
                if (title.isBlank() || desktop == null) {
                    continue;
                }
                Map<String, Object> view = new LinkedHashMap<>();
                view.put("key", text(item, "key"));
                view.put("title", title);
                view.put("subtitle", text(item, "subtitle"));
                view.put("desktopImage", desktop);
                view.put("mobileImage", media(item.get("mobileMediaId"), mediaMap, text(item, "imageAlt")));
                view.put("buttonText", text(item, "buttonText"));
                view.put("_link", item.get("link"));
                items.add(view);
            }
        }
        sortByOrder(block == null ? null : block.get("items"), items);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        return result;
    }

    /** 首屏排序按 sortOrder，再按 key 字典序稳定。 */
    private void sortByOrder(JsonNode source, List<Map<String, Object>> items) {
        if (source == null || !source.isArray() || items.size() < 2) {
            return;
        }
        Map<String, Integer> orders = new HashMap<>();
        for (JsonNode item : source) {
            orders.put(text(item, "key"), item.get("sortOrder") == null ? 100 : item.get("sortOrder").asInt());
        }
        items.sort((a, b) -> {
            int left = orders.getOrDefault(String.valueOf(a.get("key")), 100);
            int right = orders.getOrDefault(String.valueOf(b.get("key")), 100);
            if (left != right) {
                return Integer.compare(left, right);
            }
            return String.valueOf(a.get("key")).compareTo(String.valueOf(b.get("key")));
        });
    }

    private void enrichHeroLinks(Map<String, Object> hero, boolean formEnabled, Set<String> anchors) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) hero.get("items");
        for (Map<String, Object> item : items) {
            Object raw = item.remove("_link");
            Map<String, Object> action = links.resolve((JsonNode) raw, null, formEnabled, anchors);
            item.put("link", action);
            if (action == null) {
                item.put("buttonText", "");
            }
            // 稳定字段顺序：把 link 移到 buttonText 之后即可，Map 顺序不影响 JSON 语义
            item.putIfAbsent("link", null);
        }
    }

    // -------------------------------------------------- metrics

    private Map<String, Object> metrics(JsonNode block) {
        if (!enabled(block)) {
            return null;
        }
        List<Map<String, Object>> items = new ArrayList<>();
        JsonNode list = block == null ? null : block.get("items");
        if (list != null && list.isArray()) {
            for (JsonNode item : list) {
                String value = text(item, "displayValue");
                String label = text(item, "label");
                if (value.isBlank() || label.isBlank()) {
                    continue;
                }
                Map<String, Object> view = new LinkedHashMap<>();
                view.put("key", text(item, "key"));
                view.put("displayValue", value);
                view.put("unit", text(item, "unit"));
                view.put("label", label);
                view.put("description", text(item, "description"));
                items.add(view);
            }
        }
        if (items.isEmpty()) {
            return null;
        }
        sortByOrder(list, items);
        Map<String, Object> result = blockView(block);
        result.put("items", items);
        return result;
    }

    // -------------------------------------------------- services

    private Map<String, Object> services(JsonNode block, Map<Long, ContentRow> contentMap,
                                          Map<Long, MediaRow> mediaMap, boolean formEnabled) {
        if (!enabled(block)) {
            return null;
        }
        List<Map<String, Object>> groups = new ArrayList<>();
        JsonNode list = block == null ? null : block.get("groups");
        if (list != null && list.isArray()) {
            for (JsonNode group : list) {
                String contentType = text(group, "contentType");
                List<Map<String, Object>> cards = new ArrayList<>();
                JsonNode ids = group.get("contentIds");
                if (ids != null && ids.isArray()) {
                    for (JsonNode idNode : ids) {
                        ContentRow content = contentMap.get(idNode.asLong());
                        if (content == null || !content.contentType().name().equals(contentType)) {
                            continue;
                        }
                        cards.add(card(content, mediaMap));
                    }
                }
                if (cards.isEmpty()) {
                    continue;
                }
                Map<String, Object> view = new LinkedHashMap<>();
                view.put("key", text(group, "key"));
                view.put("title", text(group, "title"));
                view.put("description", text(group, "description"));
                view.put("image", media(group.get("imageMediaId"), mediaMap, text(group, "title")));
                view.put("moreLink", links.resolve(group.get("moreLink"), null, formEnabled, Set.of()));
                view.put("items", cards);
                groups.add(view);
            }
        }
        if (groups.isEmpty()) {
            return null;
        }
        sortByOrder(list, groups);
        Map<String, Object> result = blockView(block);
        result.put("groups", groups);
        return result;
    }

    private Map<String, Object> card(ContentRow content, Map<Long, MediaRow> mediaMap) {
        JsonNode json = content.published();
        Map<String, Object> card = new LinkedHashMap<>();
        String id = String.valueOf(content.id());
        card.put("id", id);
        card.put("contentType", content.contentType().name());
        card.put("title", text(json, "title"));
        card.put("summary", text(json, "summary"));
        card.put("cover", cover(json, mediaMap));
        card.put("url", contentUrl(content));
        return card;
    }

    private String contentUrl(ContentRow content) {
        String base = switch (content.contentType()) {
            case PRODUCT -> "/products/";
            case SOLUTION -> "/solutions/";
            case CASE -> "/cases/";
            case NEWS -> "/news/";
            default -> null;
        };
        return base == null ? null : base + content.id();
    }

    // -------------------------------------------------- cases

    private Map<String, Object> cases(JsonNode block, Map<Long, ContentRow> contentMap,
                                      Map<Long, MediaRow> mediaMap) {
        if (!enabled(block)) {
            return null;
        }
        List<Map<String, Object>> items = new ArrayList<>();
        JsonNode ids = block == null ? null : block.get("contentIds");
        if (ids != null && ids.isArray()) {
            for (JsonNode idNode : ids) {
                ContentRow content = contentMap.get(idNode.asLong());
                if (content == null || content.contentType() != com.company.cms.content.ContentType.CASE) {
                    continue;
                }
                JsonNode json = content.published();
                Map<String, Object> view = card(content, mediaMap);
                view.put("customerName", text(json, "customerName"));
                view.put("industry", text(json, "industry"));
                JsonNode outcomes = json == null ? null : json.get("outcomes");
                view.put("outcomes", outcomes == null || outcomes.isNull() ? List.of() : outcomes);
                items.add(view);
            }
        }
        if (items.isEmpty()) {
            return null;
        }
        Map<String, Object> result = blockView(block);
        result.put("items", items);
        result.put("moreUrl", "/cases");
        return result;
    }

    // -------------------------------------------------- customers

    private Map<String, Object> customers(JsonNode block, Map<Long, ContentRow> contentMap,
                                          Map<Long, MediaRow> mediaMap) {
        if (!enabled(block)) {
            return null;
        }
        List<Map<String, Object>> items = new ArrayList<>();
        JsonNode ids = block == null ? null : block.get("contentIds");
        if (ids != null && ids.isArray()) {
            for (JsonNode idNode : ids) {
                ContentRow content = contentMap.get(idNode.asLong());
                if (content == null || content.contentType() != com.company.cms.content.ContentType.CUSTOMER) {
                    continue;
                }
                JsonNode json = content.published();
                Map<String, Object> logo = media(json == null ? null : json.get("logoMediaId"), mediaMap,
                        text(json, "logoAlt").isBlank() ? text(json, "title") : text(json, "logoAlt"));
                if (logo == null) {
                    continue;
                }
                Map<String, Object> view = new LinkedHashMap<>();
                view.put("id", String.valueOf(content.id()));
                view.put("name", text(json, "title"));
                view.put("logo", logo);
                String website = text(json, "websiteUrl");
                view.put("url", website.isBlank() ? null : website);
                items.add(view);
            }
        }
        if (items.isEmpty()) {
            return null;
        }
        Map<String, Object> result = blockView(block);
        result.put("items", items);
        return result;
    }

    // -------------------------------------------------- news

    private Map<String, Object> news(JsonNode block, Map<Long, ContentRow> contentMap, Map<Long, MediaRow> mediaMap) {
        if (!enabled(block)) {
            return null;
        }
        String mode = text(block, "mode").isEmpty() ? "SELECTED" : text(block, "mode");
        int limit = block != null && block.get("limit") != null ? Math.max(1, Math.min(6, block.get("limit").asInt())) : 3;
        List<Map<String, Object>> items = new ArrayList<>();
        if ("LATEST".equals(mode)) {
            for (ContentRow content : contents.findLatestPublishedNews(limit)) {
                items.add(newsItem(content, mediaMap));
            }
        } else {
            JsonNode ids = block == null ? null : block.get("contentIds");
            if (ids != null && ids.isArray()) {
                for (JsonNode idNode : ids) {
                    if (items.size() >= limit) {
                        break;
                    }
                    ContentRow content = contentMap.get(idNode.asLong());
                    if (content == null || content.contentType() != com.company.cms.content.ContentType.NEWS) {
                        continue;
                    }
                    items.add(newsItem(content, mediaMap));
                }
            }
        }
        if (items.isEmpty()) {
            return null;
        }
        Map<String, Object> result = blockView(block);
        result.put("mode", mode);
        result.put("items", items);
        result.put("moreUrl", "/news");
        return result;
    }

    private Map<String, Object> newsItem(ContentRow content, Map<Long, MediaRow> mediaMap) {
        JsonNode json = content.published();
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", String.valueOf(content.id()));
        item.put("title", text(json, "title"));
        item.put("summary", text(json, "summary"));
        item.put("displayTime", text(json, "displayTime"));
        item.put("url", "/news/" + content.id());
        item.put("cover", cover(json, mediaMap));
        return item;
    }

    // -------------------------------------------------- consultation

    private Map<String, Object> consultation(JsonNode block, boolean formEnabled, Set<String> anchors) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("title", text(block, "title"));
        view.put("subtitle", text(block, "subtitle"));
        String buttonText = text(block, "buttonText");
        Map<String, Object> action = links.resolve(block == null ? null : block.get("link"),
                null, formEnabled, anchors);
        if (action == null) {
            // 表单关闭或目标失效时退到联系页，不留空按钮
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("type", "PAGE");
            fallback.put("url", "/contact");
            action = fallback;
            if (buttonText.isBlank()) {
                buttonText = "联系我们";
            }
        }
        view.put("buttonText", buttonText);
        view.put("link", action);
        return view;
    }

    // -------------------------------------------------- 工具

    private Map<String, Object> blockView(JsonNode block) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("title", text(block, "title"));
        view.put("subtitle", text(block, "subtitle"));
        return view;
    }

    private boolean enabled(JsonNode block) {
        return block != null && (block.get("enabled") == null || block.get("enabled").asBoolean(true));
    }

    private Map<String, Object> cover(JsonNode json, Map<Long, MediaRow> mediaMap) {
        JsonNode idNode = json == null ? null : json.get("coverMediaId");
        String alt = text(json, "coverAlt");
        if (alt.isBlank()) {
            alt = text(json, "title");
        }
        return media(idNode, mediaMap, alt);
    }

    private Map<String, Object> media(JsonNode idNode, Map<Long, MediaRow> mediaMap, String alt) {
        if (idNode == null || !idNode.isNumber()) {
            return null;
        }
        MediaRow media = mediaMap.get(idNode.asLong());
        if (media == null) {
            return null;
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", String.valueOf(media.id()));
        view.put("url", "/api/public/media/" + media.id());
        view.put("alt", alt == null ? "" : alt);
        view.put("width", media.width());
        view.put("height", media.height());
        view.put("type", media.mediaType());
        view.put("durationMs", media.durationMs());
        return view;
    }

    private boolean formEnabled() {
        return configs.find(SiteConfigKey.CONTACT)
                .map(SiteConfigRow::published)
                .map(json -> json.get("formEnabled") != null && json.get("formEnabled").asBoolean(false))
                .orElse(false);
    }

    private String text(JsonNode node, String field) {
        if (node == null) {
            return "";
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asString().trim();
    }

}
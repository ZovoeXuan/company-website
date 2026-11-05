package com.company.cms.site;

import com.company.cms.common.ApiException;
import com.company.cms.common.ErrorCode;
import com.company.cms.common.FieldErrorItem;
import com.company.cms.common.HtmlSanitizer;
import com.company.cms.content.ContentRepository;
import com.company.cms.content.ContentRow;
import com.company.cms.media.MediaService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 配置草稿校验与归一化（文档 6.1～6.4）。
 *
 * 保存草稿只校验结构、长度、数组上限、资源存在与链接安全；必填与「目标已发布」
 * 放到发布校验，保证运营可以分多次填完。
 */
@Component
public class SiteConfigValidator {

    private static final Set<String> PAGE_CODES = Set.of(
            "HOME", "PRODUCTS", "SOLUTIONS", "CASES", "NEWS", "ABOUT", "CONTACT", "PRIVACY");

    private final ObjectMapper objectMapper;
    private final HtmlSanitizer htmlSanitizer;
    private final LinkResolver links;
    private final MediaService mediaService;
    private final ContentRepository contents;

    public SiteConfigValidator(ObjectMapper objectMapper, HtmlSanitizer htmlSanitizer,
                               LinkResolver links, MediaService mediaService,
                               ContentRepository contents) {
        this.objectMapper = objectMapper;
        this.htmlSanitizer = htmlSanitizer;
        this.links = links;
        this.mediaService = mediaService;
        this.contents = contents;
    }

    public JsonNode validate(SiteConfigKey key, JsonNode draft) {
        if (draft == null || !draft.isObject()) {
            throw ApiException.validation("draft", "配置内容必须是 JSON 对象");
        }
        List<FieldErrorItem> errors = new ArrayList<>();
        ObjectNode normalized = switch (key) {
            case SITE -> site(draft, errors);
            case NAVIGATION -> navigation(draft, errors);
            case ABOUT -> about(draft, errors);
            case CONTACT -> contact(draft, errors);
            case PRIVACY -> privacy(draft, errors);
            case HOME -> home(draft, errors);
        };
        if (!errors.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "请检查配置内容", errors);
        }
        return normalized;
    }

    // ---------------------------------------------------------------- SITE

    private ObjectNode site(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("siteName", limit(text(draft, "siteName"), 60, "siteName", "站点名称", errors));
        node.put("slogan", limit(text(draft, "slogan"), 100, "slogan", "标语", errors));
        // 旧字段 copyright 仍写入，公开接口取 copyrightText 优先
        String copyright = limit(text(draft, "copyrightText"), 120, "copyrightText", "版权文案", errors);
        if (copyright.isEmpty()) {
            copyright = limit(text(draft, "copyright"), 120, "copyrightText", "版权文案", errors);
        }
        node.put("copyrightText", copyright);
        node.put("icpText", limit(text(draft, "icpText"), 80, "icpText", "备案号", errors));
        String icpUrl = externalOrEmpty(draft, "icpUrl", "icpUrl", "备案链接", errors);
        node.put("icpUrl", icpUrl);
        node.put("policeText", limit(text(draft, "policeText"), 80, "policeText", "公安备案号", errors));
        node.put("policeUrl", externalOrEmpty(draft, "policeUrl", "policeUrl", "公安备案链接", errors));

        Long logoId = mediaId(draft.get("logoMediaId"), "logoMediaId", "Logo", errors);
        if (logoId != null) {
            node.put("logoMediaId", logoId);
        }
        node.put("logoAlt", limit(text(draft, "logoAlt"), 120, "logoAlt", "Logo 替代文字", errors));
        Long faviconId = mediaId(draft.get("faviconMediaId"), "faviconMediaId", "站点图标", errors);
        if (faviconId != null) {
            node.put("faviconMediaId", faviconId);
        }

        JsonNode seo = draft.get("defaultSeo");
        ObjectNode seoNode = objectMapper.createObjectNode();
        seoNode.put("title", limit(text(seo, "title"), 80, "defaultSeo.title", "默认标题", errors));
        seoNode.put("description", limit(text(seo, "description"), 200, "defaultSeo.description", "默认描述", errors));
        node.set("defaultSeo", seoNode);

        ArrayNode groups = objectMapper.createArrayNode();
        JsonNode footerGroups = draft.get("footerGroups");
        if (footerGroups != null && footerGroups.isArray()) {
            if (footerGroups.size() > 4) {
                errors.add(new FieldErrorItem("footerGroups", "页脚分组最多 4 组"));
            }
            for (int i = 0; i < Math.min(footerGroups.size(), 4); i++) {
                JsonNode group = footerGroups.get(i);
                String path = "footerGroups[" + i + "]";
                ObjectNode item = objectMapper.createObjectNode();
                String title = limit(text(group, "title"), 20, path + ".title", "分组标题", errors);
                if (title.isEmpty()) {
                    errors.add(new FieldErrorItem(path + ".title", "请填写分组标题"));
                }
                item.put("title", title);
                item.set("links", navLinks(group.get("links"), path + ".links", 10, errors));
                groups.add(item);
            }
        }
        node.set("footerGroups", groups);

        // 联系方式统一在 CONTACT 维护，这里不再接受重复录入
        return node;
    }

    // ---------------------------------------------------------- NAVIGATION

    private ObjectNode navigation(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        JsonNode items = draft.get("items");
        ArrayNode result = objectMapper.createArrayNode();
        if (items == null || !items.isArray() || items.isEmpty()) {
            errors.add(new FieldErrorItem("items", "请至少配置一项导航"));
        } else {
            if (items.size() > 8) {
                errors.add(new FieldErrorItem("items", "顶级导航最多 8 项"));
            }
            Set<String> labels = new HashSet<>();
            Set<String> keys = new HashSet<>();
            for (int i = 0; i < Math.min(items.size(), 8); i++) {
                JsonNode item = items.get(i);
                String path = "items[" + i + "]";
                if (item == null || !item.isObject()) {
                    errors.add(new FieldErrorItem(path, "格式不正确"));
                    continue;
                }
                ObjectNode nav = objectMapper.createObjectNode();
                String key = stableKey(item, "nav", "key-" + i, keys, path, errors);
                nav.put("key", key);
                String label = limit(text(item, "label"), 20, path + ".label", "导航名称", errors);
                if (label.isEmpty()) {
                    errors.add(new FieldErrorItem(path + ".label", "请填写导航名称"));
                }
                if (!labels.add(label)) {
                    errors.add(new FieldErrorItem(path + ".label", "同级导航名称不能重复"));
                }
                nav.put("label", label);
                nav.put("enabled", bool(item, "enabled", true));
                nav.put("sortOrder", sortOrder(item, path, errors));
                nav.set("link", linkOrNull(item.get("link"), path + ".link", "导航链接", errors));

                JsonNode children = item.get("children");
                ArrayNode childNodes = objectMapper.createArrayNode();
                if (children != null && children.isArray() && !children.isEmpty()) {
                    if (children.size() > 10) {
                        errors.add(new FieldErrorItem(path + ".children", "子菜单最多 10 项"));
                    }
                    Set<String> childLabels = new HashSet<>();
                    Set<String> childKeys = new HashSet<>();
                    for (int j = 0; j < Math.min(children.size(), 10); j++) {
                        JsonNode child = children.get(j);
                        String childPath = path + ".children[" + j + "]";
                        if (child == null || !child.isObject()) {
                            errors.add(new FieldErrorItem(childPath, "格式不正确"));
                            continue;
                        }
                        ObjectNode childNode = objectMapper.createObjectNode();
                        childNode.put("key", stableKey(child, "nav", "key-c" + i + "-" + j,
                                childKeys, childPath, errors));
                        String childLabel = limit(text(child, "label"), 20,
                                childPath + ".label", "子菜单名称", errors);
                        if (childLabel.isEmpty()) {
                            errors.add(new FieldErrorItem(childPath + ".label", "请填写子菜单名称"));
                        }
                        if (!childLabels.add(childLabel)) {
                            errors.add(new FieldErrorItem(childPath + ".label", "同级导航名称不能重复"));
                        }
                        childNode.put("label", childLabel);
                        childNode.put("enabled", bool(child, "enabled", true));
                        childNode.put("sortOrder", sortOrder(child, childPath, errors));
                        childNode.set("link", linkOrNull(child.get("link"),
                                childPath + ".link", "子菜单链接", errors));
                        childNodes.add(childNode);
                    }
                }
                nav.set("children", childNodes);
                result.add(nav);
            }
        }
        node.set("items", result);

        // 产品/解决方案 5列全景下拉大菜单配置
        ArrayNode megaResult = objectMapper.createArrayNode();
        JsonNode mega = draft.get("megaColumns");
        if (mega != null && mega.isArray()) {
            if (mega.size() > 8) {
                errors.add(new FieldErrorItem("megaColumns", "大菜单列数最多 8 列"));
            }
            for (int i = 0; i < Math.min(mega.size(), 8); i++) {
                JsonNode col = mega.get(i);
                String path = "megaColumns[" + i + "]";
                if (col == null || !col.isObject()) continue;
                ObjectNode colNode = objectMapper.createObjectNode();
                String colTitle = limit(text(col, "title"), 30, path + ".title", "大菜单列标题", errors);
                colNode.put("title", colTitle);

                ArrayNode colItems = objectMapper.createArrayNode();
                JsonNode subItems = col.get("items");
                if (subItems != null && subItems.isArray()) {
                    if (subItems.size() > 15) {
                        errors.add(new FieldErrorItem(path + ".items", "每列最多 15 个项目"));
                    }
                    for (int j = 0; j < Math.min(subItems.size(), 15); j++) {
                        JsonNode sub = subItems.get(j);
                        String subPath = path + ".items[" + j + "]";
                        if (sub == null || !sub.isObject()) continue;
                        ObjectNode subNode = objectMapper.createObjectNode();
                        String label = limit(text(sub, "label"), 40, subPath + ".label", "项目名称", errors);
                        if (label.isEmpty()) {
                            errors.add(new FieldErrorItem(subPath + ".label", "请填写项目名称"));
                        }
                        subNode.put("label", label);
                        subNode.put("path", limit(text(sub, "path"), 255, subPath + ".path", "链接路径", errors));
                        subNode.put("tag", limit(text(sub, "tag"), 10, subPath + ".tag", "角标文案", errors));
                        subNode.put("isVideo", bool(sub, "isVideo", false));
                        subNode.put("videoUrl", limit(text(sub, "videoUrl"), 500, subPath + ".videoUrl", "视频链接", errors));
                        subNode.put("videoTitle", limit(text(sub, "videoTitle"), 100, subPath + ".videoTitle", "视频标题", errors));
                        colItems.add(subNode);
                    }
                }
                colNode.set("items", colItems);
                megaResult.add(colNode);
            }
        }
        node.set("megaColumns", megaResult);

        // 底部 CTA 引导条
        JsonNode cta = draft.get("megaCta");
        if (cta != null && cta.isObject()) {
            ObjectNode ctaNode = objectMapper.createObjectNode();
            ctaNode.put("text", limit(text(cta, "text"), 100, "megaCta.text", "底部提示语", errors));
            ctaNode.put("linkText", limit(text(cta, "linkText"), 50, "megaCta.linkText", "底部链接文字", errors));
            node.set("megaCta", ctaNode);
        }

        return node;
    }

    /** 页脚链接沿用导航项结构，但不允许二级。 */
    private ArrayNode navLinks(JsonNode source, String field, int max, List<FieldErrorItem> errors) {
        ArrayNode result = objectMapper.createArrayNode();
        if (source == null || !source.isArray()) {
            return result;
        }
        if (source.size() > max) {
            errors.add(new FieldErrorItem(field, "链接最多 " + max + " 项"));
        }
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < Math.min(source.size(), max); i++) {
            JsonNode item = source.get(i);
            String path = field + "[" + i + "]";
            if (item == null || !item.isObject()) {
                errors.add(new FieldErrorItem(path, "格式不正确"));
                continue;
            }
            ObjectNode node = objectMapper.createObjectNode();
            node.put("key", stableKey(item, "lnk", "key-l" + i, keys, path, errors));
            String label = limit(text(item, "label"), 20, path + ".label", "链接名称", errors);
            if (label.isEmpty()) {
                errors.add(new FieldErrorItem(path + ".label", "请填写链接名称"));
            }
            node.put("label", label);
            node.put("enabled", bool(item, "enabled", true));
            node.put("sortOrder", sortOrder(item, path, errors));
            node.set("link", linkOrNull(item.get("link"), path + ".link", "链接", errors));
            result.add(node);
        }
        return result;
    }

    // --------------------------------------------------------------- ABOUT

    private ObjectNode about(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("intro", limit(text(draft, "intro"), 2000, "intro", "公司简介", errors));
        node.put("mission", limit(text(draft, "mission"), 2000, "mission", "使命", errors));

        ArrayNode milestones = objectMapper.createArrayNode();
        JsonNode source = draft.get("milestones");
        if (source != null && source.isArray()) {
            if (source.size() > 20) {
                errors.add(new FieldErrorItem("milestones", "发展历程最多 20 条"));
            }
            for (int i = 0; i < Math.min(source.size(), 20); i++) {
                JsonNode item = source.get(i);
                String path = "milestones[" + i + "]";
                String year = text(item, "year");
                if (!year.matches("^\\d{4}$")) {
                    errors.add(new FieldErrorItem(path + ".year", "年份需为 4 位数字"));
                }
                ObjectNode milestone = objectMapper.createObjectNode();
                milestone.put("year", year);
                milestone.put("title", limit(text(item, "title"), 50, path + ".title", "标题", errors));
                milestone.put("description",
                        limit(text(item, "description"), 300, path + ".description", "说明", errors));
                milestones.add(milestone);
            }
        }
        node.set("milestones", milestones);

        ArrayNode qualifications = objectMapper.createArrayNode();
        JsonNode quals = draft.get("qualifications");
        if (quals != null && quals.isArray()) {
            if (quals.size() > 20) {
                errors.add(new FieldErrorItem("qualifications", "资质最多 20 条"));
            }
            for (int i = 0; i < Math.min(quals.size(), 20); i++) {
                JsonNode item = quals.get(i);
                String path = "qualifications[" + i + "]";
                ObjectNode qual = objectMapper.createObjectNode();
                qual.put("name", limit(text(item, "name"), 60, path + ".name", "名称", errors));
                qual.put("description",
                        limit(text(item, "description"), 300, path + ".description", "说明", errors));
                qualifications.add(qual);
            }
        }
        node.set("qualifications", qualifications);
        return node;
    }

    // ------------------------------------------------------------- CONTACT

    private ObjectNode contact(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        String title = limit(text(draft, "title"), 50, "title", "页面标题", errors);
        node.put("title", title.isEmpty() ? "联系我们" : title);
        node.put("description", limit(text(draft, "description"), 300, "description", "页面描述", errors));
        node.put("successText", limit(text(draft, "successText"), 200, "successText", "提交成功提示", errors));
        node.put("formEnabled", bool(draft, "formEnabled", false));
        node.put("phone", limit(text(draft, "phone"), 40, "phone", "联系电话", errors));
        String email = limit(text(draft, "email"), 100, "email", "邮箱", errors);
        if (!email.isEmpty() && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            errors.add(new FieldErrorItem("email", "邮箱格式不正确"));
        }
        node.put("email", email);
        node.put("address", limit(text(draft, "address"), 120, "address", "地址", errors));
        node.put("hours", limit(text(draft, "hours"), 60, "hours", "服务时间", errors));
        return node;
    }

    // ------------------------------------------------------------- PRIVACY

    private ObjectNode privacy(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        String updatedAt = text(draft, "updatedAt");
        try {
            node.put("updatedAt", LocalDate.parse(updatedAt).toString());
        } catch (DateTimeParseException ex) {
            errors.add(new FieldErrorItem("updatedAt", "更新日期格式需为 yyyy-MM-dd"));
            node.put("updatedAt", LocalDate.now().toString());
        }
        String body = htmlSanitizer.sanitize(text(draft, "bodyHtml"));
        if (body.isBlank()) {
            errors.add(new FieldErrorItem("bodyHtml", "隐私说明正文不能为空"));
        }
        node.put("bodyHtml", body);
        return node;
    }

    // ---------------------------------------------------------------- HOME

    private ObjectNode home(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.set("hero", hero(draft.get("hero"), errors));
        node.set("metrics", metrics(draft.get("metrics"), errors));
        node.set("services", services(draft.get("services"), errors));
        node.set("cases", caseBlock(draft.get("cases"), errors));
        node.set("customers", customers(draft.get("customers"), errors));
        node.set("news", news(draft.get("news"), errors));
        node.set("consultation", consultation(draft.get("consultation"), errors));
        return node;
    }

    private ObjectNode hero(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        ArrayNode items = objectMapper.createArrayNode();
        JsonNode list = source == null ? null : source.get("items");
        if (list != null && list.isArray()) {
            if (list.size() > 5) {
                errors.add(new FieldErrorItem("hero.items", "首屏最多 5 条"));
            }
            Set<String> keys = new HashSet<>();
            for (int i = 0; i < Math.min(list.size(), 5); i++) {
                JsonNode item = list.get(i);
                String path = "hero.items[" + i + "]";
                if (item == null || !item.isObject()) {
                    errors.add(new FieldErrorItem(path, "格式不正确"));
                    continue;
                }
                ObjectNode heroItem = objectMapper.createObjectNode();
                heroItem.put("key", stableKey(item, "hero", "key-h" + i, keys, path, errors));
                heroItem.put("enabled", bool(item, "enabled", true));
                heroItem.put("sortOrder", sortOrder(item, path, errors));
                heroItem.put("title", limit(text(item, "title"), 40, path + ".title", "首屏标题", errors));
                heroItem.put("subtitle",
                        limit(text(item, "subtitle"), 100, path + ".subtitle", "首屏副标题", errors));
                Long desktop = mediaId(item.get("desktopMediaId"), path + ".desktopMediaId", "桌面图片", errors);
                if (desktop != null) {
                    heroItem.put("desktopMediaId", desktop);
                }
                Long mobile = mediaId(item.get("mobileMediaId"), path + ".mobileMediaId", "手机图片", errors);
                if (mobile != null) {
                    heroItem.put("mobileMediaId", mobile);
                }
                heroItem.put("imageAlt", limit(text(item, "imageAlt"), 120, path + ".imageAlt", "图片替代文字", errors));
                String buttonText = limit(text(item, "buttonText"), 12, path + ".buttonText", "按钮文案", errors);
                ObjectNode link = linkOrNull(item.get("link"), path + ".link", "按钮链接", errors);
                if (!buttonText.isEmpty() && link == null) {
                    errors.add(new FieldErrorItem(path + ".link", "填写按钮文案时必须配置链接"));
                }
                if (buttonText.isEmpty() && link != null && !"NONE".equals(text(link, "type"))) {
                    errors.add(new FieldErrorItem(path + ".buttonText", "配置链接时必须填写按钮文案"));
                }
                heroItem.put("buttonText", buttonText);
                heroItem.set("link", link);
                items.add(heroItem);
            }
        }
        node.set("items", items);
        return node;
    }

    private ObjectNode metrics(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = block(source, "metrics", "企业指标", errors);
        ArrayNode items = objectMapper.createArrayNode();
        JsonNode list = source == null ? null : source.get("items");
        if (list != null && list.isArray()) {
            if (list.size() > 8) {
                errors.add(new FieldErrorItem("metrics.items", "指标最多 8 条"));
            }
            Set<String> keys = new HashSet<>();
            for (int i = 0; i < Math.min(list.size(), 8); i++) {
                JsonNode item = list.get(i);
                String path = "metrics.items[" + i + "]";
                if (item == null || !item.isObject()) {
                    errors.add(new FieldErrorItem(path, "格式不正确"));
                    continue;
                }
                ObjectNode metric = objectMapper.createObjectNode();
                metric.put("key", stableKey(item, "metric", "key-m" + i, keys, path, errors));
                metric.put("sortOrder", sortOrder(item, path, errors));
                metric.put("displayValue",
                        limit(text(item, "displayValue"), 16, path + ".displayValue", "展示值", errors));
                metric.put("unit", limit(text(item, "unit"), 8, path + ".unit", "单位", errors));
                metric.put("label", limit(text(item, "label"), 20, path + ".label", "指标名称", errors));
                metric.put("description",
                        limit(text(item, "description"), 80, path + ".description", "指标说明", errors));
                items.add(metric);
            }
        }
        node.set("items", items);
        return node;
    }

    private ObjectNode services(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = block(source, "services", "产品服务", errors);
        ArrayNode groups = objectMapper.createArrayNode();
        JsonNode list = source == null ? null : source.get("groups");
        if (list != null && list.isArray()) {
            if (list.size() > 3) {
                errors.add(new FieldErrorItem("services.groups", "服务分组最多 3 组"));
            }
            Set<String> keys = new HashSet<>();
            for (int i = 0; i < Math.min(list.size(), 3); i++) {
                JsonNode group = list.get(i);
                String path = "services.groups[" + i + "]";
                if (group == null || !group.isObject()) {
                    errors.add(new FieldErrorItem(path, "格式不正确"));
                    continue;
                }
                ObjectNode item = objectMapper.createObjectNode();
                item.put("key", stableKey(group, "group", "key-g" + i, keys, path, errors));
                item.put("sortOrder", sortOrder(group, path, errors));
                item.put("title", limit(text(group, "title"), 30, path + ".title", "分组标题", errors));
                item.put("description",
                        limit(text(group, "description"), 200, path + ".description", "分组说明", errors));
                Long image = mediaId(group.get("imageMediaId"), path + ".imageMediaId", "分组配图", errors);
                if (image != null) {
                    item.put("imageMediaId", image);
                }
                String contentType = text(group, "contentType");
                if (!Set.of("PRODUCT", "SOLUTION").contains(contentType)) {
                    errors.add(new FieldErrorItem(path + ".contentType", "分组内容类型只能是产品或解决方案"));
                }
                item.put("contentType", contentType);
                ArrayNode ids = idArray(group.get("contentIds"), path + ".contentIds", 6, errors);
                if (ids.isEmpty()) {
                    errors.add(new FieldErrorItem(path + ".contentIds", "请选择 1 到 6 个内容"));
                }
                item.set("contentIds", ids);
                item.set("moreLink", linkOrNull(group.get("moreLink"), path + ".moreLink", "更多链接", errors));
                groups.add(item);
            }
        }
        node.set("groups", groups);
        return node;
    }

    private ObjectNode caseBlock(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = block(source, "cases", "客户案例", errors);
        node.set("contentIds", idArray(source == null ? null : source.get("contentIds"),
                "cases.contentIds", 6, errors));
        return node;
    }

    private ObjectNode customers(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = block(source, "customers", "客户标识", errors);
        node.set("contentIds", idArray(source == null ? null : source.get("contentIds"),
                "customers.contentIds", 30, errors));
        return node;
    }

    private ObjectNode news(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = block(source, "news", "新闻动态", errors);
        String mode = text(source, "mode");
        if (mode.isEmpty()) {
            mode = "SELECTED";
        }
        if (!Set.of("SELECTED", "LATEST").contains(mode)) {
            errors.add(new FieldErrorItem("news.mode", "新闻模式只能是 SELECTED 或 LATEST"));
            mode = "SELECTED";
        }
        node.put("mode", mode);
        int limit = intValue(source, "limit", 3);
        if (limit < 1 || limit > 6) {
            errors.add(new FieldErrorItem("news.limit", "条数需在 1 到 6 之间"));
            limit = Math.max(1, Math.min(6, limit));
        }
        node.put("limit", limit);
        node.set("contentIds", idArray(source == null ? null : source.get("contentIds"),
                "news.contentIds", 6, errors));
        return node;
    }

    private ObjectNode consultation(JsonNode source, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("title", limit(text(source, "title"), 40, "consultation.title", "咨询区标题", errors));
        node.put("subtitle",
                limit(text(source, "subtitle"), 200, "consultation.subtitle", "咨询区副标题", errors));
        node.put("buttonText",
                limit(text(source, "buttonText"), 12, "consultation.buttonText", "按钮文案", errors));
        node.set("link", linkOrNull(source == null ? null : source.get("link"),
                "consultation.link", "咨询动作", errors));
        return node;
    }

    /** 区块公共字段：enabled、title、subtitle。 */
    private ObjectNode block(JsonNode source, String field, String label, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("enabled", bool(source, "enabled", true));
        node.put("title", limit(text(source, "title"), 40, field + ".title", label + "标题", errors));
        node.put("subtitle", limit(text(source, "subtitle"), 200, field + ".subtitle", label + "副标题", errors));
        return node;
    }

    // ----------------------------------------------------------- 发布校验

    /**
     * 发布前的完整校验：必填、目标存在且已发布、类型匹配。
     * 与保存校验分开，避免运营填一半就被拦住。
     */
    public void validateForPublish(SiteConfigKey key, JsonNode draft) {
        List<FieldErrorItem> errors = new ArrayList<>();
        switch (key) {
            case HOME -> publishHome(draft, errors);
            case NAVIGATION -> publishNavigation(draft, errors);
            case SITE -> publishSite(draft, errors);
            case CONTACT -> publishContact(draft, errors);
            default -> {
            }
        }
        if (!errors.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "发布前请先补全以下内容", errors);
        }
    }

    private void publishSite(JsonNode draft, List<FieldErrorItem> errors) {
        if (text(draft, "siteName").isBlank()) {
            errors.add(new FieldErrorItem("siteName", "站点名称不能为空"));
        }
        if (text(draft, "copyrightText").isBlank()) {
            errors.add(new FieldErrorItem("copyrightText", "版权文案不能为空"));
        }
        JsonNode seo = draft.get("defaultSeo");
        if (text(seo, "title").isBlank()) {
            errors.add(new FieldErrorItem("defaultSeo.title", "默认标题不能为空"));
        }
        if (text(seo, "description").isBlank()) {
            errors.add(new FieldErrorItem("defaultSeo.description", "默认描述不能为空"));
        }
        String icpText = text(draft, "icpText");
        String icpUrl = text(draft, "icpUrl");
        if (!icpText.isEmpty() && icpUrl.isEmpty()) {
            errors.add(new FieldErrorItem("icpUrl", "填写备案号时需同时填写备案链接"));
        }
        if (icpText.isEmpty() && !icpUrl.isEmpty()) {
            errors.add(new FieldErrorItem("icpText", "填写备案链接时需同时填写备案号"));
        }
    }

    private void publishContact(JsonNode draft, List<FieldErrorItem> errors) {
        if (text(draft, "phone").isBlank() && text(draft, "email").isBlank()) {
            errors.add(new FieldErrorItem("phone", "联系电话与邮箱至少填写一项"));
        }
    }

    private void publishNavigation(JsonNode draft, List<FieldErrorItem> errors) {
        JsonNode items = draft.get("items");
        if (items == null || !items.isArray() || items.isEmpty()) {
            errors.add(new FieldErrorItem("items", "请至少配置一项导航"));
            return;
        }
        java.util.Map<String, Boolean> rendered = new java.util.HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            JsonNode item = items.get(i);
            String path = "items[" + i + "]";
            boolean enabled = item.get("enabled") == null || item.get("enabled").asBoolean(true);
            if (!enabled) {
                continue;
            }
            JsonNode children = item.get("children");
            boolean hasChild = false;
            if (children != null && children.isArray()) {
                for (int j = 0; j < children.size(); j++) {
                    JsonNode child = children.get(j);
                    boolean childEnabled = child.get("enabled") == null || child.get("enabled").asBoolean(true);
                    if (!childEnabled) {
                        continue;
                    }
                    if (!links.publishable(child.get("link"), null)) {
                        errors.add(new FieldErrorItem(path + ".children[" + j + "].link",
                                "子菜单目标不存在或未发布"));
                    } else {
                        hasChild = true;
                    }
                }
            }
            String type = text(item.get("link"), "type");
            if (!hasChild) {
                if (!links.publishable(item.get("link"), null) || "NONE".equals(type)) {
                    errors.add(new FieldErrorItem(path + ".link", "导航项必须有有效链接或有效子菜单"));
                }
            }
            rendered.put(path, hasChild || !"NONE".equals(type));
        }
        if (rendered.values().stream().noneMatch(Boolean::booleanValue)) {
            errors.add(new FieldErrorItem("items", "没有可渲染的导航项"));
        }
    }

    private void publishHome(JsonNode draft, List<FieldErrorItem> errors) {
        JsonNode hero = draft.get("hero");
        JsonNode items = hero == null ? null : hero.get("items");
        int validHero = 0;
        if (items != null && items.isArray()) {
            for (int i = 0; i < items.size(); i++) {
                JsonNode item = items.get(i);
                boolean enabled = item.get("enabled") == null || item.get("enabled").asBoolean(true);
                if (!enabled) {
                    continue;
                }
                String path = "hero.items[" + i + "]";
                if (text(item, "title").isBlank()) {
                    errors.add(new FieldErrorItem(path + ".title", "首屏标题不能为空"));
                }
                if (item.get("desktopMediaId") == null) {
                    errors.add(new FieldErrorItem(path + ".desktopMediaId", "启用中的首屏必须有桌面图片"));
                } else if (!mediaExists(item.get("desktopMediaId").asLong())) {
                    errors.add(new FieldErrorItem(path + ".desktopMediaId", "桌面图片不存在"));
                }
                if (!links.publishable(item.get("link"), null)) {
                    errors.add(new FieldErrorItem(path + ".link", "按钮目标不存在或未发布"));
                }
                validHero++;
            }
        }
        if (validHero == 0) {
            errors.add(new FieldErrorItem("hero.items", "正式上线至少需要 1 条有效首屏"));
        }

        // metrics 无引用
        JsonNode services = draft.get("services");
        if (services != null && services.get("groups") != null && services.get("groups").isArray()) {
            for (int i = 0; i < services.get("groups").size(); i++) {
                JsonNode group = services.get("groups").get(i);
                String path = "services.groups[" + i + "]";
                if (text(group, "title").isBlank()) {
                    errors.add(new FieldErrorItem(path + ".title", "分组标题不能为空"));
                }
                String contentType = text(group, "contentType");
                JsonNode ids = group.get("contentIds");
                if (ids == null || ids.isEmpty()) {
                    errors.add(new FieldErrorItem(path + ".contentIds", "分组至少选择 1 个内容"));
                } else {
                    for (int j = 0; j < ids.size(); j++) {
                        long id = ids.get(j).asLong();
                        var row = contents.findPublishedById(id);
                        if (row.isEmpty() || !row.get().contentType().name().equals(contentType)) {
                            errors.add(new FieldErrorItem(path + ".contentIds[" + j + "]",
                                    "内容不存在、未发布或类型不匹配"));
                        }
                    }
                }
                if (!links.publishable(group.get("moreLink"), null)) {
                    errors.add(new FieldErrorItem(path + ".moreLink", "更多链接目标不存在或未发布"));
                }
            }
        }

        publishIdBlock(draft, "cases", "CASE", 6, errors);
        publishIdBlock(draft, "customers", "CUSTOMER", 30, errors);
        JsonNode news = draft.get("news");
        if (news != null && "SELECTED".equals(text(news, "mode"))) {
            publishIdBlock(draft, "news", "NEWS", 6, errors);
        }

        JsonNode consultation = draft.get("consultation");
        if (consultation != null) {
            if (text(consultation, "title").isBlank()) {
                errors.add(new FieldErrorItem("consultation.title", "咨询区标题不能为空"));
            }
            if (text(consultation, "buttonText").isBlank()) {
                errors.add(new FieldErrorItem("consultation.buttonText", "咨询按钮文案不能为空"));
            }
            links.publishable(consultation.get("link"), null);
            String type = text(consultation.get("link"), "type");
            if (!"CONSULT".equals(type) && !"PAGE".equals(type)) {
                errors.add(new FieldErrorItem("consultation.link", "咨询区按钮只能指向咨询动作或联系页"));
            } else if ("PAGE".equals(type) && !"CONTACT".equals(text(consultation.get("link"), "pageCode"))) {
                errors.add(new FieldErrorItem("consultation.link", "咨询区按钮只能指向联系页"));
            }
        }
    }

    private void publishIdBlock(JsonNode draft, String field, String expectedType, int max,
                                List<FieldErrorItem> errors) {
        JsonNode block = draft.get(field);
        if (block == null) {
            return;
        }
        JsonNode ids = block.get("contentIds");
        if (ids == null || !ids.isArray()) {
            return;
        }
        if (ids.size() > max) {
            errors.add(new FieldErrorItem(field + ".contentIds", "最多选择 " + max + " 项"));
        }
        for (int i = 0; i < ids.size(); i++) {
            long id = ids.get(i).asLong();
            var row = contents.findPublishedById(id);
            if (row.isEmpty()) {
                errors.add(new FieldErrorItem(field + ".contentIds[" + i + "]", "内容不存在或未发布"));
            } else if (!row.get().contentType().name().equals(expectedType)) {
                errors.add(new FieldErrorItem(field + ".contentIds[" + i + "]", "内容类型不匹配"));
            }
        }
    }

    // -------------------------------------------------------------- 工具

    private ObjectNode linkOrNull(JsonNode raw, String path, String label, List<FieldErrorItem> errors) {
        LinkResolver.SeedErrors seed = new LinkResolver.SeedErrors();
        ObjectNode node = links.normalize(raw, path, label, seed);
        errors.addAll(seed.items());
        return node;
    }

    private Long mediaId(JsonNode node, String path, String label, List<FieldErrorItem> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        long id;
        if (node.isNumber()) {
            id = node.asLong();
        } else {
            try {
                id = Long.parseLong(node.asString());
            } catch (NumberFormatException ex) {
                errors.add(new FieldErrorItem(path, label + "不合法"));
                return null;
            }
        }
        if (id <= 0) {
            errors.add(new FieldErrorItem(path, label + "不合法"));
            return null;
        }
        if (!mediaExists(id)) {
            errors.add(new FieldErrorItem(path, label + "不存在，请重新选择"));
            return null;
        }
        return id;
    }

    private boolean mediaExists(long id) {
        try {
            mediaService.require(id);
            return true;
        } catch (ApiException ex) {
            return false;
        }
    }

    private ArrayNode idArray(JsonNode source, String path, int max, List<FieldErrorItem> errors) {
        ArrayNode result = objectMapper.createArrayNode();
        if (source == null || !source.isArray()) {
            return result;
        }
        if (source.size() > max) {
            errors.add(new FieldErrorItem(path, "最多选择 " + max + " 项"));
        }
        Set<Long> seen = new HashSet<>();
        for (int i = 0; i < Math.min(source.size(), max); i++) {
            JsonNode node = source.get(i);
            Long id = node.isNumber() ? node.asLong() : parseLong(node.asString());
            if (id == null || id <= 0) {
                errors.add(new FieldErrorItem(path + "[" + i + "]", "内容 ID 不合法"));
                continue;
            }
            if (!seen.add(id)) {
                errors.add(new FieldErrorItem(path + "[" + i + "]", "同一内容不能重复选择"));
                continue;
            }
            result.add(id);
        }
        return result;
    }

    private Long parseLong(String raw) {
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String stableKey(JsonNode item, String prefix, String fallback,
                             Set<String> used, String path, List<FieldErrorItem> errors) {
        String key = text(item, "key");
        if (key.isEmpty() || !key.matches("^[a-z0-9-]{4,40}$") || used.contains(key)) {
            key = fallback + "-" + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x10000, 0xfffff));
            while (used.contains(key)) {
                key = prefix + "-" + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x10000, 0xfffff));
            }
        }
        used.add(key);
        return key;
    }

    private int sortOrder(JsonNode item, String path, List<FieldErrorItem> errors) {
        int value = intValue(item, "sortOrder", 100);
        if (value < 0 || value > 9999) {
            errors.add(new FieldErrorItem(path + ".sortOrder", "排序需在 0 到 9999 之间"));
            return 100;
        }
        return value;
    }

    private int intValue(JsonNode node, String field, int fallback) {
        if (node == null) {
            return fallback;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return fallback;
        }
        return value.isNumber() ? value.asInt() : fallback;
    }

    private boolean bool(JsonNode node, String field, boolean fallback) {
        if (node == null) {
            return fallback;
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? fallback : value.asBoolean(fallback);
    }

    private String externalOrEmpty(JsonNode node, String field, String path, String label,
                                   List<FieldErrorItem> errors) {
        String url = text(node, field).trim();
        if (url.isEmpty()) {
            return "";
        }
        String lower = url.toLowerCase();
        if (!lower.startsWith("http://") && !lower.startsWith("https://") || url.length() > 500) {
            errors.add(new FieldErrorItem(path, label + "必须是 http/https 完整地址"));
            return "";
        }
        return url;
    }

    private String text(JsonNode node, String field) {
        if (node == null) {
            return "";
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return "";
        }
        return value.isString() ? value.asString().trim() : value.asString();
    }

    private String limit(String value, int max, String path, String label, List<FieldErrorItem> errors) {
        if (value.length() > max) {
            errors.add(new FieldErrorItem(path, label + "不能超过 " + max + " 字"));
        }
        return value;
    }
}

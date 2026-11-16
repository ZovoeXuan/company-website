package com.company.cms.site;

import com.company.cms.common.ApiException;
import com.company.cms.common.ErrorCode;
import com.company.cms.common.FieldErrorItem;
import com.company.cms.common.HtmlSanitizer;
import com.company.cms.content.ContentRepository;
import com.company.cms.content.ContentRow;
import com.company.cms.content.ContentType;
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
 * 站点配置草稿校验与正向白名单归一化：
 * - 移除 AI 助手 (xiaoqiConfig) 及所有未经校验的透传对象
 * - 完整保留联系页 (companyName, formTitle, smsVerifyEnabled, mapUrl, qrMediaId)
 * - 发布时调用 LinkResolver.publishable 及关联内容校验，杜绝死链上线
 */
@Component
public class SiteConfigValidator {

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

    // -------------------------------------------------- SITE

    private ObjectNode site(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("siteName", limit(text(draft, "siteName"), 60, "siteName", "站点名称", errors));
        node.put("slogan", limit(text(draft, "slogan"), 100, "slogan", "标语", errors));
        String copyright = limit(text(draft, "copyrightText"), 120, "copyrightText", "版权文案", errors);
        if (copyright.isEmpty()) {
            copyright = limit(text(draft, "copyright"), 120, "copyrightText", "版权文案", errors);
        }
        node.put("copyrightText", copyright);
        node.put("icpText", limit(text(draft, "icpText"), 80, "icpText", "备案号", errors));
        node.put("icpUrl", externalOrEmpty(draft, "icpUrl", "icpUrl", "备案链接", errors));
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

        // 页脚受控二维码列表（仅保存受控 mediaId 或合法 URL）
        ArrayNode qrCodes = objectMapper.createArrayNode();
        JsonNode rawQrs = draft.get("qrCodes");
        if (rawQrs != null && rawQrs.isArray()) {
            for (int i = 0; i < Math.min(rawQrs.size(), 4); i++) {
                JsonNode q = rawQrs.get(i);
                if (q == null || !q.isObject()) continue;
                String path = "qrCodes[" + i + "]";
                ObjectNode qNode = objectMapper.createObjectNode();
                qNode.put("label", limit(text(q, "label"), 20, path + ".label", "二维码名称", errors));
                Long qMediaId = mediaId(q.get("mediaId"), path + ".mediaId", "二维码图片", errors);
                if (qMediaId != null) {
                    qNode.put("mediaId", qMediaId);
                    qNode.put("imageUrl", "/api/public/media/" + qMediaId);
                }
                qrCodes.add(qNode);
            }
        }
        node.set("qrCodes", qrCodes);

        // 页脚多邮箱职能列表（结构化白名单）
        ArrayNode contactEmails = objectMapper.createArrayNode();
        JsonNode rawEmails = draft.get("contactEmails");
        if (rawEmails != null && rawEmails.isArray()) {
            for (int i = 0; i < Math.min(rawEmails.size(), 6); i++) {
                JsonNode em = rawEmails.get(i);
                if (em == null || !em.isObject()) continue;
                String path = "contactEmails[" + i + "]";
                String emailAddr = limit(text(em, "email"), 100, path + ".email", "联系邮箱", errors);
                if (emailAddr.isEmpty()) continue;
                if (!emailAddr.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                    errors.add(new FieldErrorItem(path + ".email", "邮箱格式不正确"));
                }
                ObjectNode emNode = objectMapper.createObjectNode();
                emNode.put("email", emailAddr);
                emNode.put("desc", limit(text(em, "desc"), 40, path + ".desc", "邮箱用途说明", errors));
                contactEmails.add(emNode);
            }
        }
        node.set("contactEmails", contactEmails);

        // 栏目首屏横幅配置（结构化白名单）
        JsonNode rawHeroes = draft.get("pageHeroes");
        if (rawHeroes != null && rawHeroes.isObject()) {
            ObjectNode heroesNode = objectMapper.createObjectNode();
            for (String pageKey : List.of("products", "solutions", "cases", "news")) {
                JsonNode ph = rawHeroes.get(pageKey);
                if (ph != null && ph.isObject()) {
                    String path = "pageHeroes." + pageKey;
                    ObjectNode phNode = objectMapper.createObjectNode();
                    phNode.put("title", limit(text(ph, "title"), 60, path + ".title", "栏目标题", errors));
                    phNode.put("subtitle", limit(text(ph, "subtitle"), 200, path + ".subtitle", "栏目副标题", errors));
                    phNode.put("eyebrow", limit(text(ph, "eyebrow"), 40, path + ".eyebrow", "栏目眉题", errors));
                    String bgType = text(ph, "bgType");
                    if (!Set.of("default", "image", "video").contains(bgType)) {
                        bgType = "default";
                    }
                    phNode.put("bgType", bgType);
                    phNode.put("bgImage", limit(text(ph, "bgImage"), 500, path + ".bgImage", "背景图地址", errors));
                    phNode.put("bgVideo", limit(text(ph, "bgVideo"), 500, path + ".bgVideo", "背景视频地址", errors));
                    double opacity = ph.get("overlayOpacity") != null && ph.get("overlayOpacity").isNumber()
                            ? Math.max(0.0, Math.min(1.0, ph.get("overlayOpacity").asDouble())) : 0.55;
                    phNode.put("overlayOpacity", opacity);
                    heroesNode.set(pageKey, phNode);
                }
            }
            node.set("pageHeroes", heroesNode);
        }

        return node;
    }

    // -------------------------------------------------- NAVIGATION

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

        ArrayNode megaResult = objectMapper.createArrayNode();
        JsonNode mega = draft.get("megaColumns");
        if (mega != null && mega.isArray()) {
            for (int i = 0; i < Math.min(mega.size(), 8); i++) {
                JsonNode col = mega.get(i);
                String path = "megaColumns[" + i + "]";
                if (col == null || !col.isObject()) continue;
                ObjectNode colNode = objectMapper.createObjectNode();
                colNode.put("title", limit(text(col, "title"), 30, path + ".title", "大菜单列标题", errors));

                ArrayNode colItems = objectMapper.createArrayNode();
                JsonNode subItems = col.get("items");
                if (subItems != null && subItems.isArray()) {
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

        JsonNode cta = draft.get("megaCta");
        if (cta != null && cta.isObject()) {
            ObjectNode ctaNode = objectMapper.createObjectNode();
            ctaNode.put("text", limit(text(cta, "text"), 100, "megaCta.text", "底部提示语", errors));
            ctaNode.put("linkText", limit(text(cta, "linkText"), 50, "megaCta.linkText", "底部链接文字", errors));
            node.set("megaCta", ctaNode);
        }

        return node;
    }

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

    // -------------------------------------------------- ABOUT

    private ObjectNode about(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("heroTitle", limit(text(draft, "heroTitle"), 100, "heroTitle", "首屏标题", errors));
        node.put("heroSubtitle", limit(text(draft, "heroSubtitle"), 500, "heroSubtitle", "首屏副标题", errors));
        node.put("heroEyebrow", limit(text(draft, "heroEyebrow"), 60, "heroEyebrow", "首屏眉标", errors));
        String heroBgType = text(draft, "heroBgType");
        node.put("heroBgType", Set.of("default", "image", "video").contains(heroBgType) ? heroBgType : "default");
        node.put("heroBgImage", limit(text(draft, "heroBgImage"), 500, "heroBgImage", "首屏背景图", errors));
        node.put("heroBgVideo", limit(text(draft, "heroBgVideo"), 500, "heroBgVideo", "首屏背景视频", errors));
        node.put("showHeroLogoMark", bool(draft, "showHeroLogoMark", false));

        node.put("showIntro", bool(draft, "showIntro", true));
        boolean hasIntroImg = !text(draft, "introImage").isEmpty();
        node.put("showIntroImage", bool(draft, "showIntroImage", hasIntroImg));
        String defaultMode = hasIntroImg ? "image" : "none";
        node.put("introImageMode", text(draft, "introImageMode").isEmpty() ? defaultMode : text(draft, "introImageMode"));
        node.put("showCeoLetter", bool(draft, "showCeoLetter", true));
        node.put("showQualifications", bool(draft, "showQualifications", true));
        node.put("showMilestones", bool(draft, "showMilestones", true));
        node.put("showOffices", bool(draft, "showOffices", true));

        node.put("introTitle", limit(text(draft, "introTitle"), 60, "introTitle", "简介标题", errors));
        node.put("intro", limit(text(draft, "intro"), 2000, "intro", "公司简介", errors));
        node.put("introImage", limit(text(draft, "introImage"), 500, "introImage", "简介图片", errors));
        node.put("mission", limit(text(draft, "mission"), 2000, "mission", "使命", errors));
        node.put("vision", limit(text(draft, "vision"), 500, "vision", "愿景", errors));
        node.put("teamTitle", limit(text(draft, "teamTitle"), 60, "teamTitle", "团队标题", errors));
        node.put("resourcesTitle", limit(text(draft, "resourcesTitle"), 60, "resourcesTitle", "资料库标题", errors));
        node.put("joinTitle", limit(text(draft, "joinTitle"), 60, "joinTitle", "招聘横幅标题", errors));
        node.put("joinButtonText", limit(text(draft, "joinButtonText"), 20, "joinButtonText", "招聘按钮文字", errors));
        node.put("joinUrl", limit(text(draft, "joinUrl"), 500, "joinUrl", "招聘链接", errors));

        // CEO 致信结构化白名单
        JsonNode rawCeo = draft.get("ceoLetter");
        if (rawCeo != null && rawCeo.isObject()) {
            ObjectNode ceoNode = objectMapper.createObjectNode();
            ceoNode.put("leftQuote", limit(text(rawCeo, "leftQuote"), 300, "ceoLetter.leftQuote", "左侧引言", errors));
            ceoNode.put("title", limit(text(rawCeo, "title"), 100, "ceoLetter.title", "致辞标题", errors));
            ceoNode.put("signature", limit(text(rawCeo, "signature"), 50, "ceoLetter.signature", "署名", errors));
            ceoNode.put("buttonText", limit(text(rawCeo, "buttonText"), 20, "ceoLetter.buttonText", "按钮文字", errors));
            ceoNode.set("excerpt", stringList(rawCeo.get("excerpt"), 6, 500, "ceoLetter.excerpt", "摘要段落", errors));
            ceoNode.set("fullParagraphs", stringList(rawCeo.get("fullParagraphs"), 15, 1000, "ceoLetter.fullParagraphs", "全文段落", errors));
            node.set("ceoLetter", ceoNode);
        }

        // 管理团队结构化白名单
        ArrayNode teamList = objectMapper.createArrayNode();
        JsonNode rawTeam = draft.get("teamMembers");
        if (rawTeam != null && rawTeam.isArray()) {
            for (int i = 0; i < Math.min(rawTeam.size(), 20); i++) {
                JsonNode m = rawTeam.get(i);
                if (m == null || !m.isObject()) continue;
                String path = "teamMembers[" + i + "]";
                String name = limit(text(m, "name"), 40, path + ".name", "成员姓名", errors);
                if (name.isEmpty()) continue;
                ObjectNode mNode = objectMapper.createObjectNode();
                mNode.put("name", name);
                mNode.put("role", limit(text(m, "role"), 60, path + ".role", "职位", errors));
                mNode.put("avatar", limit(text(m, "avatar"), 500, path + ".avatar", "头像地址", errors));
                mNode.set("bio", stringList(m.get("bio"), 10, 600, path + ".bio", "履历段落", errors));
                teamList.add(mNode);
            }
        }
        node.set("teamMembers", teamList);

        // 资料库结构化白名单
        ArrayNode resList = objectMapper.createArrayNode();
        JsonNode rawRes = draft.get("resources");
        if (rawRes != null && rawRes.isArray()) {
            for (int i = 0; i < Math.min(rawRes.size(), 30); i++) {
                JsonNode r = rawRes.get(i);
                if (r == null || !r.isObject()) continue;
                String path = "resources[" + i + "]";
                String rTitle = limit(text(r, "title"), 80, path + ".title", "资料标题", errors);
                if (rTitle.isEmpty()) continue;
                ObjectNode rNode = objectMapper.createObjectNode();
                rNode.put("category", limit(text(r, "category"), 30, path + ".category", "资料分类", errors));
                rNode.put("title", rTitle);
                rNode.put("logoText", limit(text(r, "logoText"), 50, path + ".logoText", "封面文字", errors));
                rNode.put("logoColor", limit(text(r, "logoColor"), 30, path + ".logoColor", "封面底色", errors));
                rNode.put("url", limit(text(r, "url"), 500, path + ".url", "资料链接", errors));
                rNode.put("isVideo", bool(r, "isVideo", false));
                resList.add(rNode);
            }
        }
        node.set("resources", resList);

        // 客户故事结构化白名单
        ArrayNode storyList = objectMapper.createArrayNode();
        JsonNode rawStories = draft.get("customerStories");
        if (rawStories != null && rawStories.isArray()) {
            for (int i = 0; i < Math.min(rawStories.size(), 20); i++) {
                JsonNode st = rawStories.get(i);
                if (st == null || !st.isObject()) continue;
                String path = "customerStories[" + i + "]";
                String quote = limit(text(st, "quote"), 400, path + ".quote", "证言内容", errors);
                if (quote.isEmpty()) continue;
                ObjectNode stNode = objectMapper.createObjectNode();
                stNode.put("quote", quote);
                stNode.put("author", limit(text(st, "author"), 60, path + ".author", "证言人", errors));
                stNode.put("company", limit(text(st, "company"), 60, path + ".company", "客户公司", errors));
                stNode.put("logoText", limit(text(st, "logoText"), 40, path + ".logoText", "标识文字", errors));
                stNode.put("videoUrl", limit(text(st, "videoUrl"), 500, path + ".videoUrl", "视频地址", errors));
                storyList.add(stNode);
            }
        }
        node.set("customerStories", storyList);

        // 价值观结构化白名单
        ArrayNode valList = objectMapper.createArrayNode();
        JsonNode rawVals = draft.get("values");
        if (rawVals != null && rawVals.isArray()) {
            for (int i = 0; i < Math.min(rawVals.size(), 10); i++) {
                JsonNode v = rawVals.get(i);
                if (v == null || !v.isObject()) continue;
                String path = "values[" + i + "]";
                String vTitle = limit(text(v, "title"), 40, path + ".title", "价值观标题", errors);
                if (vTitle.isEmpty()) continue;
                ObjectNode vNode = objectMapper.createObjectNode();
                vNode.put("title", vTitle);
                vNode.set("lines", stringList(v.get("lines"), 6, 200, path + ".lines", "价值观诠释", errors));
                valList.add(vNode);
            }
        }
        node.set("values", valList);

        // 全国分支网点结构化白名单
        ArrayNode officeList = objectMapper.createArrayNode();
        JsonNode rawOffices = draft.get("offices");
        if (rawOffices != null && rawOffices.isArray()) {
            for (int i = 0; i < Math.min(rawOffices.size(), 20); i++) {
                JsonNode o = rawOffices.get(i);
                if (o == null || !o.isObject()) continue;
                String path = "offices[" + i + "]";
                String city = limit(text(o, "city"), 40, path + ".city", "城市名称", errors);
                if (city.isEmpty()) continue;
                ObjectNode oNode = objectMapper.createObjectNode();
                oNode.put("city", city);
                oNode.put("phone", limit(text(o, "phone"), 40, path + ".phone", "分部电话", errors));
                oNode.put("address", limit(text(o, "address"), 150, path + ".address", "分部地址", errors));
                oNode.put("image", limit(text(o, "image"), 500, path + ".image", "分部配图", errors));
                oNode.put("mapX", Math.max(0, Math.min(100, intValue(o, "mapX", 65))));
                oNode.put("mapY", Math.max(0, Math.min(100, intValue(o, "mapY", 50))));
                officeList.add(oNode);
            }
        }
        node.set("offices", officeList);

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
            for (int i = 0; i < Math.min(quals.size(), 20); i++) {
                JsonNode item = quals.get(i);
                String path = "qualifications[" + i + "]";
                ObjectNode qual = objectMapper.createObjectNode();
                qual.put("name", limit(text(item, "name"), 60, path + ".name", "名称", errors));
                qual.put("description",
                        limit(text(item, "description"), 300, path + ".description", "说明", errors));
                Long qMedia = mediaId(item.get("mediaId"), path + ".mediaId", "资质图片", errors);
                if (qMedia != null) {
                    qual.put("mediaId", qMedia);
                    qual.put("imageUrl", "/api/public/media/" + qMedia);
                }
                qualifications.add(qual);
            }
        }
        node.set("qualifications", qualifications);
        return node;
    }

    // -------------------------------------------------- CONTACT

    private ObjectNode contact(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        String title = limit(text(draft, "title"), 50, "title", "页面标题", errors);
        node.put("title", title.isEmpty() ? "联系我们" : title);
        node.put("companyName", limit(text(draft, "companyName"), 100, "companyName", "公司全称", errors));
        node.put("description", limit(text(draft, "description"), 300, "description", "页面描述", errors));
        String formTitle = limit(text(draft, "formTitle"), 40, "formTitle", "弹窗表单标题", errors);
        node.put("formTitle", formTitle.isEmpty() ? "预约产品展示" : formTitle);
        node.put("successText", limit(text(draft, "successText"), 200, "successText", "提交成功提示", errors));
        node.put("formEnabled", bool(draft, "formEnabled", true));
        node.put("smsVerifyEnabled", bool(draft, "smsVerifyEnabled", true));
        node.put("phone", limit(text(draft, "phone"), 40, "phone", "联系电话", errors));
        String email = limit(text(draft, "email"), 100, "email", "邮箱", errors);
        if (!email.isEmpty() && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            errors.add(new FieldErrorItem("email", "邮箱格式不正确"));
        }
        node.put("email", email);
        node.put("address", limit(text(draft, "address"), 200, "address", "地址", errors));
        node.put("hours", limit(text(draft, "hours"), 100, "hours", "服务时间", errors));
        node.put("mapUrl", externalOrEmpty(draft, "mapUrl", "mapUrl", "地图链接", errors));
        Long qrMediaId = mediaId(draft.get("qrMediaId"), "qrMediaId", "联系二维码", errors);
        if (qrMediaId != null) {
            node.put("qrMediaId", qrMediaId);
            node.put("qrImageUrl", "/api/public/media/" + qrMediaId);
        }
        node.put("qrLabel", limit(text(draft, "qrLabel"), 30, "qrLabel", "二维码说明", errors));
        return node;
    }

    // -------------------------------------------------- PRIVACY

    private ObjectNode privacy(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        String title = limit(text(draft, "title"), 50, "title", "隐私说明标题", errors);
        node.put("title", title.isEmpty() ? "隐私说明" : title);
        String updatedAt = text(draft, "updatedAt");
        try {
            node.put("updatedAt", LocalDate.parse(updatedAt).toString());
        } catch (DateTimeParseException ex) {
            errors.add(new FieldErrorItem("updatedAt", "更新日期格式需为 yyyy-MM-dd"));
            node.put("updatedAt", LocalDate.now().toString());
        }
        String rawBody = text(draft, "bodyHtml");
        if (rawBody.length() > HtmlSanitizer.MAX_LENGTH) {
            errors.add(new FieldErrorItem("bodyHtml", "隐私说明正文不能超过 " + HtmlSanitizer.MAX_LENGTH + " 字符"));
        }
        String body = htmlSanitizer.sanitize(rawBody);
        if (body.isBlank()) {
            errors.add(new FieldErrorItem("bodyHtml", "隐私说明正文不能为空"));
        }
        node.put("bodyHtml", body);
        return node;
    }

    // -------------------------------------------------- HOME

    private static final List<String> DEFAULT_HOME_SECTIONS = List.of(
            "services", "cases", "customers", "news");

    private ObjectNode home(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.set("sectionOrder", sectionOrder(draft.get("sectionOrder")));
        node.set("hero", hero(draft.get("hero"), errors));
        if (draft.get("metrics") != null && draft.get("metrics").isObject()) {
            node.set("metrics", metrics(draft.get("metrics"), errors));
        }
        node.set("services", services(draft.get("services"), errors));
        node.set("cases", caseBlock(draft.get("cases"), errors));
        node.set("customers", customers(draft.get("customers"), errors));
        node.set("news", news(draft.get("news"), errors));
        if (draft.get("consultation") != null && draft.get("consultation").isObject()) {
            node.set("consultation", consultation(draft.get("consultation"), errors));
        }
        return node;
    }

    private ArrayNode sectionOrder(JsonNode source) {
        ArrayNode arr = objectMapper.createArrayNode();
        Set<String> added = new HashSet<>();
        if (source != null && source.isArray()) {
            for (JsonNode item : source) {
                String key = item != null && item.isString() ? item.asString().trim() : "";
                if (DEFAULT_HOME_SECTIONS.contains(key) && added.add(key)) {
                    arr.add(key);
                }
            }
        }
        for (String defKey : DEFAULT_HOME_SECTIONS) {
            if (added.add(defKey)) {
                arr.add(defKey);
            }
        }
        return arr;
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
                        limit(text(item, "subtitle"), 120, path + ".subtitle", "首屏副标题", errors));
                heroItem.put("theme", "light".equals(text(item, "theme")) ? "light" : "dark");
                heroItem.put("titleColor", limit(text(item, "titleColor"), 30, path + ".titleColor", "标题颜色", errors));
                heroItem.put("subtitleColor", limit(text(item, "subtitleColor"), 30, path + ".subtitleColor", "副标题颜色", errors));
                heroItem.put("kickerColor", limit(text(item, "kickerColor"), 30, path + ".kickerColor", "眉题颜色", errors));
                heroItem.put("buttonBgColor", limit(text(item, "buttonBgColor"), 30, path + ".buttonBgColor", "按钮背景色", errors));
                heroItem.put("buttonTextColor", limit(text(item, "buttonTextColor"), 30, path + ".buttonTextColor", "按钮文字色", errors));
                heroItem.put("kicker", limit(text(item, "kicker"), 60, path + ".kicker", "眉题文案", errors));
                heroItem.put("badgeText", limit(text(item, "badgeText"), 20, path + ".badgeText", "提示文案", errors));
                Long desktop = mediaId(item.get("desktopMediaId"), path + ".desktopMediaId", "桌面图片", errors);
                if (desktop != null) {
                    heroItem.put("desktopMediaId", desktop);
                }
                Long mobile = mediaId(item.get("mobileMediaId"), path + ".mobileMediaId", "手机图片", errors);
                if (mobile != null) {
                    heroItem.put("mobileMediaId", mobile);
                }
                heroItem.put("imageAlt", limit(text(item, "imageAlt"), 120, path + ".imageAlt", "图片替代文字", errors));
                String buttonText = limit(text(item, "buttonText"), 16, path + ".buttonText", "按钮文案", errors);
                ObjectNode link = linkOrNull(item.get("link"), path + ".link", "按钮链接", errors);
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
            Set<String> keys = new HashSet<>();
            for (int i = 0; i < Math.min(list.size(), 8); i++) {
                JsonNode group = list.get(i);
                String path = "services.groups[" + i + "]";
                if (group == null || !group.isObject()) continue;
                ObjectNode item = objectMapper.createObjectNode();
                item.put("key", stableKey(group, "group", "key-g" + i, keys, path, errors));
                item.put("sortOrder", sortOrder(group, path, errors));
                item.put("title", limit(text(group, "title"), 30, path + ".title", "分组标题", errors));
                item.put("description",
                        limit(text(group, "description"), 200, path + ".description", "分组说明", errors));
                item.put("tag", limit(text(group, "tag"), 40, path + ".tag", "分组标签", errors));
                ArrayNode highlights = objectMapper.createArrayNode();
                JsonNode hNode = group.get("highlights");
                if (hNode != null && hNode.isArray()) {
                    for (int j = 0; j < Math.min(hNode.size(), 6); j++) {
                        JsonNode hItem = hNode.get(j);
                        String rawH = hItem != null && hItem.isString() ? hItem.asString().trim() : "";
                        String hText = limit(rawH, 40, path + ".highlights[" + j + "]", "亮点标签", errors);
                        if (!hText.isBlank()) {
                            highlights.add(hText);
                        }
                    }
                }
                item.set("highlights", highlights);
                String layout = text(group, "layout");
                item.put("layout", Set.of("auto", "left", "right").contains(layout) ? layout : "auto");
                Long image = mediaId(group.get("imageMediaId"), path + ".imageMediaId", "分组配图", errors);
                if (image != null) {
                    item.put("imageMediaId", image);
                }
                String contentType = text(group, "contentType");
                if (!Set.of("PRODUCT", "SOLUTION").contains(contentType)) {
                    contentType = "PRODUCT";
                }
                item.put("contentType", contentType);
                ArrayNode ids = idArray(group.get("contentIds"), path + ".contentIds", 6, errors);
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
        String mode = text(source, "mode").toUpperCase();
        if ("MANUAL".equals(mode)) {
            mode = "SELECTED";
        }
        if (!Set.of("SELECTED", "LATEST").contains(mode)) {
            mode = "LATEST";
        }
        node.put("mode", mode);
        int limit = intValue(source, "limit", 3);
        limit = Math.max(1, Math.min(6, limit));
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
                limit(text(source, "buttonText"), 16, "consultation.buttonText", "按钮文案", errors));
        node.set("link", linkOrNull(source == null ? null : source.get("link"),
                "consultation.link", "咨询动作", errors));
        return node;
    }

    private ObjectNode block(JsonNode source, String field, String label, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("enabled", bool(source, "enabled", true));
        node.put("title", limit(text(source, "title"), 40, field + ".title", label + "标题", errors));
        node.put("subtitle", limit(text(source, "subtitle"), 200, field + ".subtitle", label + "副标题", errors));
        return node;
    }

    private ArrayNode stringList(JsonNode source, int maxItems, int maxLen, String path, String label,
                                 List<FieldErrorItem> errors) {
        ArrayNode arr = objectMapper.createArrayNode();
        if (source == null || !source.isArray()) return arr;
        for (int i = 0; i < Math.min(source.size(), maxItems); i++) {
            JsonNode it = source.get(i);
            String val = it != null ? it.asString().trim() : "";
            if (!val.isEmpty()) {
                arr.add(limit(val, maxLen, path + "[" + i + "]", label, errors));
            }
        }
        return arr;
    }

    // -------------------------------------------------- 发布校验（含 LinkResolver.publishable 死链阻断）

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
        JsonNode footerGroups = draft.get("footerGroups");
        if (footerGroups != null && footerGroups.isArray()) {
            for (int i = 0; i < footerGroups.size(); i++) {
                JsonNode grpLinks = footerGroups.get(i).get("links");
                if (grpLinks != null && grpLinks.isArray()) {
                    for (int j = 0; j < grpLinks.size(); j++) {
                        JsonNode lnk = grpLinks.get(j);
                        if (bool(lnk, "enabled", true) && !links.publishable(lnk.get("link"), null)) {
                            errors.add(new FieldErrorItem("footerGroups[" + i + "].links[" + j + "].link",
                                    "页脚链接指向的目标内容不存在、尚未发布或类型不匹配"));
                        }
                    }
                }
            }
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
        for (int i = 0; i < items.size(); i++) {
            JsonNode item = items.get(i);
            if (!bool(item, "enabled", true)) continue;
            if (!links.publishable(item.get("link"), null)) {
                errors.add(new FieldErrorItem("items[" + i + "].link",
                        "导航项「" + text(item, "label") + "」指向的目标内容不存在、尚未发布或类型不匹配"));
            }
            JsonNode children = item.get("children");
            if (children != null && children.isArray()) {
                for (int j = 0; j < children.size(); j++) {
                    JsonNode child = children.get(j);
                    if (bool(child, "enabled", true) && !links.publishable(child.get("link"), null)) {
                        errors.add(new FieldErrorItem("items[" + i + "].children[" + j + "].link",
                                "子菜单「" + text(child, "label") + "」指向的目标内容不存在、尚未发布或类型不匹配"));
                    }
                }
            }
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
                if (!enabled) continue;
                String path = "hero.items[" + i + "]";
                if (text(item, "title").isBlank()) {
                    errors.add(new FieldErrorItem(path + ".title", "首屏标题不能为空"));
                }
                if (!links.publishable(item.get("link"), null)) {
                    errors.add(new FieldErrorItem(path + ".link", "首屏按钮链接指向的目标内容尚未发布或不存在"));
                }
                validHero++;
            }
        }
        if (validHero == 0) {
            errors.add(new FieldErrorItem("hero.items", "正式上线至少需要 1 条有效首屏"));
        }

        // 校验首页服务分组引用的内容与更多链接
        JsonNode services = draft.get("services");
        if (services != null && bool(services, "enabled", true)) {
            JsonNode groups = services.get("groups");
            if (groups != null && groups.isArray()) {
                for (int i = 0; i < groups.size(); i++) {
                    JsonNode g = groups.get(i);
                    String path = "services.groups[" + i + "]";
                    if (!links.publishable(g.get("moreLink"), null)) {
                        errors.add(new FieldErrorItem(path + ".moreLink", "分组更多链接指向的目标尚未发布"));
                    }
                    String expectedType = text(g, "contentType").isEmpty() ? "PRODUCT" : text(g, "contentType");
                    checkPublishedContentIds(g.get("contentIds"), expectedType, path + ".contentIds", errors);
                }
            }
        }
        JsonNode cases = draft.get("cases");
        if (cases != null && bool(cases, "enabled", true)) {
            checkPublishedContentIds(cases.get("contentIds"), "CASE", "cases.contentIds", errors);
        }
        JsonNode customers = draft.get("customers");
        if (customers != null && bool(customers, "enabled", true)) {
            checkPublishedContentIds(customers.get("contentIds"), "CUSTOMER", "customers.contentIds", errors);
        }
        JsonNode news = draft.get("news");
        if (news != null && bool(news, "enabled", true) && "SELECTED".equals(text(news, "mode"))) {
            checkPublishedContentIds(news.get("contentIds"), "NEWS", "news.contentIds", errors);
        }
    }

    private void checkPublishedContentIds(JsonNode idsNode, String expectedType, String path,
                                          List<FieldErrorItem> errors) {
        if (idsNode == null || !idsNode.isArray()) return;
        for (int i = 0; i < idsNode.size(); i++) {
            Long cid = idsNode.get(i).isNumber() ? idsNode.get(i).asLong() : parseLong(idsNode.get(i).asString());
            if (cid == null) continue;
            ContentRow row = contents.findPublishedById(cid).orElse(null);
            if (row == null || !row.contentType().name().equals(expectedType)) {
                errors.add(new FieldErrorItem(path + "[" + i + "]",
                        "引用的内容 #" + cid + " 不存在、未发布或类型不匹配"));
            }
        }
    }

    // -------------------------------------------------- 校验辅助工具

    private ObjectNode linkOrNull(JsonNode linkNode, String path, String label, List<FieldErrorItem> errors) {
        if (linkNode == null || linkNode.isNull() || !linkNode.isObject()) {
            return null;
        }
        LinkResolver.SeedErrors seed = new LinkResolver.SeedErrors();
        ObjectNode node = links.normalize(linkNode, path, label, seed);
        errors.addAll(seed.items());
        return node;
    }

    private Long mediaId(JsonNode node, String path, String label, List<FieldErrorItem> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        Long id = node.isNumber() ? node.asLong() : parseLong(node.asString());
        if (id == null || id <= 0) {
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
        Set<Long> seen = new HashSet<>();
        for (int i = 0; i < Math.min(source.size(), max); i++) {
            JsonNode node = source.get(i);
            Long id = node.isNumber() ? node.asLong() : parseLong(node.asString());
            if (id != null && id > 0 && seen.add(id)) {
                result.add(id);
            }
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
        return (value < 0 || value > 9999) ? 100 : value;
    }

    private int intValue(JsonNode node, String field, int fallback) {
        if (node == null) return fallback;
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) return fallback;
        return value.isNumber() ? value.asInt() : fallback;
    }

    private boolean bool(JsonNode node, String field, boolean fallback) {
        if (node == null) return fallback;
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? fallback : value.asBoolean(fallback);
    }

    private String externalOrEmpty(JsonNode node, String field, String path, String label,
                                    List<FieldErrorItem> errors) {
        String url = text(node, field).trim();
        if (url.isEmpty()) return "";
        String lower = url.toLowerCase();
        if (!lower.startsWith("http://") && !lower.startsWith("https://") || url.length() > 500) {
            errors.add(new FieldErrorItem(path, label + "必须是 http/https 完整地址"));
            return "";
        }
        return url;
    }

    private String text(JsonNode node, String field) {
        if (node == null) return "";
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) return "";
        return value.isString() ? value.asString().trim() : value.asString();
    }

    private String limit(String value, int max, String path, String label, List<FieldErrorItem> errors) {
        if (value.length() > max) {
            errors.add(new FieldErrorItem(path, label + "不能超过 " + max + " 字"));
        }
        return value;
    }
}

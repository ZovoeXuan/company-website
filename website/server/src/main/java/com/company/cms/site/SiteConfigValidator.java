package com.company.cms.site;

import com.company.cms.common.ApiException;
import com.company.cms.common.ErrorCode;
import com.company.cms.common.FieldErrorItem;
import com.company.cms.common.HtmlSanitizer;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置草稿校验与归一化。每种键有独立结构，统一在这里收敛，
 * 避免 Controller 里堆 if，也保证发布前一定过一遍同样的规则。
 */
@Component
public class SiteConfigValidator {

    private final ObjectMapper objectMapper;
    private final HtmlSanitizer htmlSanitizer;

    public SiteConfigValidator(ObjectMapper objectMapper, HtmlSanitizer htmlSanitizer) {
        this.objectMapper = objectMapper;
        this.htmlSanitizer = htmlSanitizer;
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

    private ObjectNode site(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("siteName", requiredText(draft, "siteName", 50, "siteName", "站点名称", errors));
        node.put("slogan", optionalText(draft, "slogan", 100, "slogan", "标语", errors));
        node.put("copyright", optionalText(draft, "copyright", 120, "copyright", "版权信息", errors));
        node.put("icp", optionalText(draft, "icp", 60, "icp", "备案号", errors));

        Long logoId = optionalLong(draft.get("logoMediaId"), "logoMediaId", "站点标识", errors, 1);
        if (logoId != null) {
            node.put("logoMediaId", logoId);
        }

        JsonNode contact = draft.get("contact");
        ObjectNode contactNode = objectMapper.createObjectNode();
        if (contact != null && contact.isObject()) {
            contactNode.put("phone", optionalText(contact, "phone", 40, "contact.phone", "联系电话", errors));
            String email = optionalText(contact, "email", 100, "contact.email", "邮箱", errors);
            if (!email.isEmpty() && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                errors.add(new FieldErrorItem("contact.email", "邮箱格式不正确"));
            }
            contactNode.put("email", email);
            contactNode.put("address", optionalText(contact, "address", 120, "contact.address", "地址", errors));
            contactNode.put("hours", optionalText(contact, "hours", 60, "contact.hours", "服务时间", errors));
        } else {
            contactNode.put("phone", "");
            contactNode.put("email", "");
            contactNode.put("address", "");
            contactNode.put("hours", "");
        }
        node.set("contact", contactNode);
        return node;
    }

    private ObjectNode navigation(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.set("items", links(draft.get("items"), "items", 12, "导航", errors));
        node.set("footerLinks", links(draft.get("footerLinks"), "footerLinks", 6, "页脚链接", errors));
        return node;
    }

    private ArrayNode links(JsonNode source, String field, int max, String label,
                            List<FieldErrorItem> errors) {
        ArrayNode result = objectMapper.createArrayNode();
        if (source == null || !source.isArray()) {
            errors.add(new FieldErrorItem(field, "请至少配置一项" + label));
            return result;
        }
        if (source.size() > max) {
            errors.add(new FieldErrorItem(field, label + "最多 " + max + " 项"));
        }
        for (int i = 0; i < Math.min(source.size(), max); i++) {
            JsonNode item = source.get(i);
            String path = "".equals(field) ? field : field + "[" + i + "]";
            if (item == null || !item.isObject()) {
                errors.add(new FieldErrorItem(path, "格式不正确"));
                continue;
            }
            String linkLabel = text(item, "label").trim();
            if (linkLabel.isEmpty() || linkLabel.length() > 12) {
                errors.add(new FieldErrorItem(path + ".label", "名称长度需在 1 到 12 之间"));
            }
            String linkPath = text(item, "path").trim();
            if (!linkPath.startsWith("/") || linkPath.contains("://") || linkPath.length() > 120) {
                errors.add(new FieldErrorItem(path + ".path", "路径必须以 / 开头，且不允许外链"));
            }
            ObjectNode link = objectMapper.createObjectNode();
            link.put("label", linkLabel);
            link.put("path", linkPath);
            link.put("visible", item.get("visible") == null || item.get("visible").asBoolean(true));
            result.add(link);
        }
        return result;
    }

    private ObjectNode about(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("intro", optionalText(draft, "intro", 2000, "intro", "公司简介", errors));
        node.put("mission", optionalText(draft, "mission", 2000, "mission", "使命", errors));

        ArrayNode milestones = objectMapper.createArrayNode();
        JsonNode source = draft.get("milestones");
        if (source != null && source.isArray()) {
            if (source.size() > 20) {
                errors.add(new FieldErrorItem("milestones", "发展历程最多 20 条"));
            }
            for (int i = 0; i < Math.min(source.size(), 20); i++) {
                JsonNode item = source.get(i);
                String path = "milestones[" + i + "]";
                String year = text(item, "year").trim();
                if (!year.matches("^\\d{4}$")) {
                    errors.add(new FieldErrorItem(path + ".year", "年份需为 4 位数字"));
                }
                ObjectNode milestone = objectMapper.createObjectNode();
                milestone.put("year", year);
                milestone.put("title", limit(text(item, "title").trim(), 50, path + ".title", "标题", errors));
                milestone.put("description",
                        limit(text(item, "description").trim(), 300, path + ".description", "说明", errors));
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
                qual.put("name", limit(text(item, "name").trim(), 60, path + ".name", "名称", errors));
                qual.put("description",
                        limit(text(item, "description").trim(), 300, path + ".description", "说明", errors));
                qualifications.add(qual);
            }
        }
        node.set("qualifications", qualifications);
        return node;
    }

    private ObjectNode contact(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("title", optionalText(draft, "title", 50, "title", "页面标题", errors));
        node.put("description", optionalText(draft, "description", 300, "description", "页面描述", errors));
        node.put("successText", optionalText(draft, "successText", 200, "successText", "提交成功提示", errors));
        node.put("formEnabled", draft.get("formEnabled") == null || draft.get("formEnabled").asBoolean(false));
        if (node.get("title").asString().isBlank()) {
            node.put("title", "联系我们");
        }
        return node;
    }

    private ObjectNode privacy(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        String updatedAt = text(draft, "updatedAt").trim();
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

    private ObjectNode home(JsonNode draft, List<FieldErrorItem> errors) {
        ObjectNode node = objectMapper.createObjectNode();
        JsonNode hero = draft.get("hero");
        if (hero == null || !hero.isObject() || text(hero, "title").isBlank()) {
            errors.add(new FieldErrorItem("hero.title", "请填写首屏标题"));
        } else {
            ObjectNode heroNode = objectMapper.createObjectNode();
            heroNode.put("eyebrow", limit(text(hero, "eyebrow").trim(), 30, "hero.eyebrow", "标签", errors));
            heroNode.put("title", limit(text(hero, "title").trim(), 60, "hero.title", "首屏标题", errors));
            heroNode.put("subtitle", limit(text(hero, "subtitle").trim(), 160, "hero.subtitle", "副标题", errors));
            Long mediaId = optionalLong(hero.get("mediaId"), "hero.mediaId", "首屏图片", errors, 1);
            if (mediaId != null) {
                heroNode.put("mediaId", mediaId);
            }
            node.set("hero", heroNode);
        }
        copyIfPresent(draft, node, "metrics", "serviceProductIds", "featuredCaseIds",
                "customerIds", "newsCount");
        return node;
    }

    private void copyIfPresent(JsonNode source, ObjectNode target, String... fields) {
        for (String field : fields) {
            JsonNode value = source.get(field);
            if (value != null && !value.isNull()) {
                target.set(field, value);
            }
        }
    }

    private String requiredText(JsonNode node, String field, int max, String path,
                                String label, List<FieldErrorItem> errors) {
        String value = text(node, field).trim();
        if (value.isEmpty()) {
            errors.add(new FieldErrorItem(path, "请填写" + label));
        } else if (value.length() > max) {
            errors.add(new FieldErrorItem(path, label + "不能超过 " + max + " 字"));
        }
        return value;
    }

    private String optionalText(JsonNode node, String field, int max, String path,
                                String label, List<FieldErrorItem> errors) {
        String value = text(node, field).trim();
        if (value.length() > max) {
            errors.add(new FieldErrorItem(path, label + "不能超过 " + max + " 字"));
        }
        return value;
    }

    private String limit(String value, int max, String path, String label, List<FieldErrorItem> errors) {
        if (value.length() > max) {
            errors.add(new FieldErrorItem(path, label + "不能超过 " + max + " 字"));
        }
        return value;
    }

    private Long optionalLong(JsonNode node, String path, String label,
                              List<FieldErrorItem> errors, long min) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isNumber() || node.asLong() < min) {
            errors.add(new FieldErrorItem(path, label + "不合法"));
            return null;
        }
        return node.asLong();
    }

    private String text(JsonNode node, String field) {
        if (node == null) {
            return "";
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asString();
    }
}

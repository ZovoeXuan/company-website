package com.company.cms.content;

import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从草稿 JSON 中提取媒体引用与内容引用。
 * 1. 字段名以 MediaId 结尾表示单个媒体，以 MediaIds 结尾表示媒体数组；
 * 2. 正文 bodyHtml 中通过 data-media-id="123" 或 /api/public/media/123 引用的受控媒体也会被登记引用，
 *    确保正文中插入的受控图片在发布后可通过公开媒体接口访问，且在素材库中受引用保护。
 * 3. 关联内容使用 relatedCaseIds / relatedProductIds / relatedSolutionIds。
 */
public final class JsonRefs {

    public record MediaRef(String path, long mediaId) {
    }

    public record ContentRef(String path, long contentId) {
    }

    private static final List<String> CONTENT_REF_FIELDS = List.of(
            "relatedCaseIds", "relatedProductIds", "relatedSolutionIds");

    private static final Pattern BODY_MEDIA_PATTERN = Pattern.compile(
            "(?:data-media-id\\s*=\\s*[\"'](\\d{1,18})[\"']|/api/(?:public|admin)/media/(\\d{1,18})(?:/file)?)");

    private JsonRefs() {
    }

    public static List<MediaRef> mediaRefs(JsonNode root) {
        List<MediaRef> refs = new ArrayList<>();
        walk(root, "", refs);
        return refs;
    }

    public static List<ContentRef> contentRefs(JsonNode root) {
        List<ContentRef> refs = new ArrayList<>();
        if (root == null) {
            return refs;
        }
        for (String field : CONTENT_REF_FIELDS) {
            JsonNode node = root.get(field);
            if (node == null || !node.isArray()) {
                continue;
            }
            for (int i = 0; i < node.size(); i++) {
                Long id = asLong(node.get(i));
                if (id != null) {
                    refs.add(new ContentRef(field + "[" + i + "]", id));
                }
            }
        }
        return refs;
    }

    private static void walk(JsonNode node, String path, List<MediaRef> refs) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            for (Map.Entry<String, JsonNode> entry : node.properties()) {
                String name = entry.getKey();
                String childPath = path.isEmpty() ? name : path + "." + name;
                JsonNode value = entry.getValue();

                if (name.endsWith("MediaId")) {
                    Long id = asLong(value);
                    if (id != null) {
                        refs.add(new MediaRef(childPath, id));
                    }
                    continue;
                }
                if (name.endsWith("MediaIds") && value.isArray()) {
                    for (int i = 0; i < value.size(); i++) {
                        Long id = asLong(value.get(i));
                        if (id != null) {
                            refs.add(new MediaRef(childPath + "[" + i + "]", id));
                        }
                    }
                    continue;
                }
                if ("bodyHtml".equals(name) && value != null && value.isString()) {
                    extractHtmlMediaRefs(value.asString(), childPath, refs);
                    continue;
                }
                walk(value, childPath, refs);
            }
            return;
        }
        if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                walk(node.get(i), path + "[" + i + "]", refs);
            }
        }
    }

    private static void extractHtmlMediaRefs(String html, String basePath, List<MediaRef> refs) {
        if (html == null || html.isBlank()) {
            return;
        }
        Matcher matcher = BODY_MEDIA_PATTERN.matcher(html);
        Set<Long> seen = new LinkedHashSet<>();
        int idx = 0;
        while (matcher.find()) {
            String raw = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (raw != null && raw.matches("^\\d{1,18}$")) {
                long id = Long.parseLong(raw);
                if (id > 0 && seen.add(id)) {
                    refs.add(new MediaRef(basePath + ".img[" + (idx++) + "]", id));
                }
            }
        }
    }

    private static Long asLong(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            long val = node.asLong();
            return val > 0 ? val : null;
        }
        if (node.isString()) {
            String text = node.asString().trim();
            if (text.matches("^\\d{1,18}$")) {
                long val = Long.parseLong(text);
                return val > 0 ? val : null;
            }
        }
        return null;
    }
}

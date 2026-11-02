package com.company.cms.content;

import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 从草稿 JSON 中提取媒体引用与内容引用。
 * 约定：字段名以 MediaId 结尾表示单个媒体，以 MediaIds 结尾表示媒体数组；
 * 关联内容使用 relatedCaseIds / relatedProductIds / relatedSolutionIds。
 */
public final class JsonRefs {

    public record MediaRef(String path, long mediaId) {
    }

    public record ContentRef(String path, long contentId) {
    }

    private static final List<String> CONTENT_REF_FIELDS = List.of(
            "relatedCaseIds", "relatedProductIds", "relatedSolutionIds");

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

    private static Long asLong(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.asLong();
        }
        if (node.isString()) {
            String text = node.asString().trim();
            if (text.matches("^\\d{1,18}$")) {
                return Long.parseLong(text);
            }
        }
        return null;
    }
}

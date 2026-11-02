package com.company.cms.content;

import com.company.cms.common.ApiException;
import com.company.cms.common.PageResult;
import com.company.cms.media.MediaRow;
import com.company.cms.media.MediaService;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 公开内容的组装：只读取发布快照，把内部字段（categoryId、封面 ID）换成前端需要的结构。
 */
@Service
public class PublicContentService {

    private final ContentRepository contents;
    private final CategoryRepository categories;
    private final MediaService mediaService;
    private final ContentRefRepository contentRefs;

    public PublicContentService(ContentRepository contents, CategoryRepository categories,
                                MediaService mediaService, ContentRefRepository contentRefs) {
        this.contents = contents;
        this.categories = categories;
        this.mediaService = mediaService;
        this.contentRefs = contentRefs;
    }

    public PageResult<Map<String, Object>> list(ContentType type, Long categoryId, String keyword,
                                                int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        boolean newsOrder = type == ContentType.NEWS;
        List<ContentRow> rows = contents.findPublished(type, categoryId, keyword, newsOrder, offset, pageSize);
        List<Map<String, Object>> items = rows.stream().map(this::toSummary).toList();
        long total = contents.countPublished(type, categoryId, keyword);
        return PageResult.of(items, page, pageSize, total);
    }

    public Map<String, Object> detail(ContentType type, long id) {
        ContentRow row = contents.findPublishedById(id)
                .filter(found -> found.contentType() == type)
                .orElseThrow(() -> ApiException.notFound("内容不存在或未发布"));
        return toDetail(row);
    }

    public List<Map<String, Object>> categories(ContentType type) {
        return categories.findPublished(type).stream()
                .map(category -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", String.valueOf(category.id()));
                    item.put("name", category.publishedName());
                    item.put("sortOrder", category.publishedSortOrder());
                    return item;
                })
                .toList();
    }

    private Map<String, Object> toSummary(ContentRow row) {
        JsonNode json = row.published();
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", String.valueOf(row.id()));
        item.put("title", text(json, "title"));
        item.put("summary", text(json, "summary"));
        item.put("cover", cover(json));
        item.put("categoryId", row.publishedCategoryId() == null ? null
                : String.valueOf(row.publishedCategoryId()));
        item.put("categoryName", categoryName(row.publishedCategoryId()));

        copyIfPresent(json, item, "industry", "results", "displayTime");
        return item;
    }

    private Map<String, Object> toDetail(ContentRow row) {
        JsonNode json = row.published();
        Map<String, Object> detail = toSummary(row);
        copyIfPresent(json, detail, "bodyHtml", "capabilities", "client", "background",
                "actions", "quote", "challenge", "approach", "outcome");

        List<Long> targets = contentRefs.findPublishedTargets("CONTENT", row.id());
        List<Map<String, Object>> relatedCases = new ArrayList<>();
        List<Map<String, Object>> relatedProducts = new ArrayList<>();
        List<Map<String, Object>> relatedSolutions = new ArrayList<>();
        for (Long targetId : targets) {
            contents.findPublishedById(targetId).ifPresent(target -> {
                Map<String, Object> summary = toSummary(target);
                switch (target.contentType()) {
                    case CASE -> relatedCases.add(summary);
                    case PRODUCT -> relatedProducts.add(summary);
                    case SOLUTION -> relatedSolutions.add(summary);
                    default -> {
                    }
                }
            });
        }
        if (!relatedCases.isEmpty()) {
            detail.put("relatedCases", relatedCases);
        }
        if (!relatedProducts.isEmpty()) {
            detail.put("relatedProducts", relatedProducts);
        }
        if (!relatedSolutions.isEmpty()) {
            detail.put("relatedSolutions", relatedSolutions);
        }

        if (row.contentType() == ContentType.NEWS) {
            contents.findAdjacentNews(row.id(), true)
                    .ifPresent(prev -> detail.put("prev", Map.of(
                            "id", String.valueOf(prev.id()), "title", titleOf(prev))));
            contents.findAdjacentNews(row.id(), false)
                    .ifPresent(next -> detail.put("next", Map.of(
                            "id", String.valueOf(next.id()), "title", titleOf(next))));
        }
        return detail;
    }

    private String titleOf(ContentRow row) {
        return row.publishedTitle() == null ? "" : row.publishedTitle();
    }

    private Map<String, Object> cover(JsonNode json) {
        Long mediaId = longOrNull(json.get("coverMediaId"));
        if (mediaId == null) {
            return null;
        }
        MediaRow media;
        try {
            media = mediaService.require(mediaId);
        } catch (ApiException ex) {
            return null;
        }
        String alt = text(json, "coverAlt");
        if (alt.isBlank()) {
            alt = text(json, "title");
        }
        Map<String, Object> cover = new LinkedHashMap<>();
        cover.put("id", String.valueOf(mediaId));
        cover.put("url", "/api/public/media/" + mediaId);
        cover.put("alt", alt);
        cover.put("width", media.width());
        cover.put("height", media.height());
        return cover;
    }

    private String categoryName(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categories.findById(categoryId).map(CategoryRow::publishedName).orElse(null);
    }

    private void copyIfPresent(JsonNode json, Map<String, Object> target, String... fields) {
        if (json == null) {
            return;
        }
        for (String field : fields) {
            JsonNode value = json.get(field);
            if (value != null && !value.isNull()) {
                target.put(field, value.isTextual() ? value.asString() : value);
            }
        }
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
        return node.isNumber() ? node.asLong() : null;
    }
}

package com.company.cms.content;

import com.company.cms.auth.CmsPrincipal;
import com.company.cms.common.ApiException;
import com.company.cms.common.ErrorCode;
import com.company.cms.common.FieldErrorItem;
import com.company.cms.common.HtmlSanitizer;
import com.company.cms.common.PageQuery;
import com.company.cms.common.PageResult;
import com.company.cms.media.MediaService;
import com.company.cms.media.MediaRefRepository;
import com.company.cms.system.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 内容核心：草稿保存、发布快照、下线、删除保护与引用维护。
 * 发布是唯一会把 draft_json 复制到 published_json 的入口。
 */
@Service
public class ContentService {

    private static final int MAX_TITLE = 120;
    private static final int MAX_SUMMARY = 300;
    private static final int MAX_BODY = 60_000;

    private final ContentRepository contents;
    private final CategoryRepository categories;
    private final MediaService mediaService;
    private final MediaRefRepository mediaRefs;
    private final ContentRefRepository contentRefs;
    private final AuditLogService audit;
    private final ObjectMapper objectMapper;
    private final HtmlSanitizer htmlSanitizer;

    public ContentService(ContentRepository contents, CategoryRepository categories,
                          MediaService mediaService, MediaRefRepository mediaRefs,
                          ContentRefRepository contentRefs, AuditLogService audit,
                          ObjectMapper objectMapper, HtmlSanitizer htmlSanitizer) {
        this.contents = contents;
        this.categories = categories;
        this.mediaService = mediaService;
        this.mediaRefs = mediaRefs;
        this.contentRefs = contentRefs;
        this.audit = audit;
        this.objectMapper = objectMapper;
        this.htmlSanitizer = htmlSanitizer;
    }

    // ---------- 查询 ----------

    public PageResult<ContentView> list(String typeRaw, String keyword, Long categoryId,
                                        String statusRaw, Boolean unpublishedChanges, PageQuery query) {
        ContentType type = typeRaw == null || typeRaw.isBlank() ? null : ContentType.parse(typeRaw);
        PublicationStatus status = statusRaw == null || statusRaw.isBlank()
                ? null : parseStatus(statusRaw);

        ContentRepository.AdminFilter filter =
                new ContentRepository.AdminFilter(type, keyword, categoryId, status, unpublishedChanges);
        List<ContentRow> rows = contents.findPage(filter, query.offset(), query.pageSize());
        long total = contents.count(filter);

        Map<Long, String> categoryNames = new HashMap<>();
        for (ContentRow row : rows) {
            Long cid = row.publishedCategoryId();
            if (cid != null && !categoryNames.containsKey(cid)) {
                categoryNames.put(cid,
                        categories.findById(cid).map(CategoryRow::publishedName).orElse(""));
            }
        }

        List<ContentView> items = rows.stream().map(row -> toView(row, categoryNames)).toList();
        return PageResult.of(items, query.page(), query.pageSize(), total);
    }

    public ContentDetailView detail(long id) {
        ContentRow row = contents.findById(id).orElseThrow(() -> ApiException.notFound("内容不存在"));
        return new ContentDetailView(toView(row, Map.of()), row.draft(), row.published());
    }

    public List<ContentRefRepository.IncomingRef> incomingRefs(long id) {
        return contentRefs.findIncoming(id);
    }

    /** 引用面板：附带来源标题，便于后台直接跳转处理。 */
    public List<Map<String, Object>> referenceViews(long id) {
        return contentRefs.findIncoming(id).stream().map(ref -> {
            String sourceTitle = contents.findById(ref.sourceId())
                    .map(source -> source.publishedTitle() != null
                            ? source.publishedTitle() : text(source.draft(), "title"))
                    .orElse("");
            Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("sourceKind", ref.sourceKind());
            item.put("sourceId", String.valueOf(ref.sourceId()));
            item.put("sourceTitle", sourceTitle);
            item.put("stage", ref.stage());
            item.put("fieldPath", ref.fieldPath());
            return item;
        }).toList();
    }

    // ---------- 写操作 ----------

    @Transactional
    public ContentDetailView create(String typeRaw, JsonNode draft, CmsPrincipal actor) {
        ContentType type = ContentType.parse(typeRaw);
        JsonNode normalized = validate(type, draft, false, null);
        long id = contents.insert(type, normalized, actor.id());
        replaceDraftRefs("CONTENT", id, normalized);
        audit.success(actor.id(), "CONTENT_CREATE", "CONTENT", id,
                "新建 " + type.name() + "：" + text(normalized, "title"));
        ContentRow row = contents.findById(id).orElseThrow();
        return new ContentDetailView(toView(row, Map.of()), row.draft(), row.published());
    }

    @Transactional
    public ContentDetailView saveDraft(long id, JsonNode draft, int expectedLockVersion, CmsPrincipal actor) {
        ContentRow row = contents.findById(id).orElseThrow(() -> ApiException.notFound("内容不存在"));
        JsonNode normalized = validate(row.contentType(), draft, false, id);

        int updated = contents.updateDraft(id, normalized, expectedLockVersion, actor.id());
        if (updated == 0) {
            throw new ApiException(ErrorCode.VERSION_CONFLICT, "内容已被他人修改，请刷新后重试");
        }
        replaceDraftRefs("CONTENT", id, normalized);

        ContentRow latest = contents.findById(id).orElseThrow();
        return new ContentDetailView(toView(latest, Map.of()), latest.draft(), latest.published());
    }

    @Transactional
    public void publish(long id, int expectedLockVersion, int expectedDraftVersion, CmsPrincipal actor) {
        ContentRow row = contents.findById(id).orElseThrow(() -> ApiException.notFound("内容不存在"));

        if (row.draftVersion() != expectedDraftVersion) {
            throw new ApiException(ErrorCode.VERSION_CONFLICT, "草稿已变化，请刷新后重新发布");
        }

        JsonNode normalized = validate(row.contentType(), row.draft(), true, id);
        Long categoryId = longOrNull(normalized.get("categoryId"));
        int sortOrder = intOrDefault(normalized.get("sortOrder"), 9999);
        LocalDateTime displayTime = displayTimeOf(normalized);

        int updated = contents.publish(id, text(normalized, "title"), categoryId, sortOrder,
                displayTime, expectedLockVersion, expectedDraftVersion, actor.id());
        if (updated == 0) {
            throw new ApiException(ErrorCode.VERSION_CONFLICT, "内容已被他人修改，请刷新后重试");
        }

        // 发布瞬间按最新草稿重算引用，再把草稿引用复制为发布引用
        replaceDraftRefs("CONTENT", id, normalized);
        mediaRefs.copyStage("CONTENT", id, "DRAFT", "PUBLISHED");
        contentRefs.copyStage("CONTENT", id, "DRAFT", "PUBLISHED");

        audit.success(actor.id(), "CONTENT_PUBLISH", "CONTENT", id,
                "发布 " + row.contentType().name() + "：" + text(normalized, "title"));
    }

    @Transactional
    public void offline(long id, int expectedLockVersion, CmsPrincipal actor) {
        ContentRow row = contents.findById(id).orElseThrow(() -> ApiException.notFound("内容不存在"));
        int updated = contents.offline(id, expectedLockVersion, actor.id());
        if (updated == 0) {
            throw new ApiException(ErrorCode.VERSION_CONFLICT, "内容状态已变化，请刷新后重试");
        }
        audit.success(actor.id(), "CONTENT_OFFLINE", "CONTENT", id,
                "下线 " + row.contentType().name() + "：" + row.publishedTitle());
    }

    @Transactional
    public void delete(long id, int expectedLockVersion, CmsPrincipal actor) {
        ContentRow row = contents.findById(id).orElseThrow(() -> ApiException.notFound("内容不存在"));
        if (row.status().isPublished()) {
            throw new ApiException(ErrorCode.REFERENCED, "已发布的内容不能直接删除，请先下线");
        }
        if (contentRefs.hasIncoming(id)) {
            throw new ApiException(ErrorCode.REFERENCED, "该内容仍被其他内容引用，请先解除引用");
        }
        int updated = contents.logicalDelete(id, expectedLockVersion);
        if (updated == 0) {
            throw new ApiException(ErrorCode.VERSION_CONFLICT, "内容已被他人修改，请刷新后重试");
        }
        mediaRefs.deleteBySource("CONTENT", id);
        contentRefs.deleteBySource("CONTENT", id);
        audit.success(actor.id(), "CONTENT_DELETE", "CONTENT", id,
                "删除 " + row.contentType().name() + "：" + text(row.draft(), "title"));
    }

    // ---------- 校验与引用 ----------

    /**
     * 草稿与发布共用一套校验；发布时额外要求分类已发布、新闻必须有展示时间。
     */
    private JsonNode validate(ContentType type, JsonNode draft, boolean forPublish, Long selfId) {
        if (draft == null || !draft.isObject()) {
            throw ApiException.validation("draft", "内容必须是 JSON 对象");
        }
        List<FieldErrorItem> errors = new ArrayList<>();
        ObjectNode normalized = objectMapper.createObjectNode();

        String title = text(draft, "title").trim();
        if (title.isEmpty() || title.length() > MAX_TITLE) {
            errors.add(new FieldErrorItem("title", "标题长度需在 1 到 " + MAX_TITLE + " 之间"));
        }
        normalized.put("title", title);

        String summary = text(draft, "summary").trim();
        if (summary.length() > MAX_SUMMARY) {
            errors.add(new FieldErrorItem("summary", "摘要不能超过 " + MAX_SUMMARY + " 字"));
        }
        normalized.put("summary", summary);

        // 正文入库前必须清洗：编辑器侧过滤可被绕过，后端是唯一可信边界
        String bodyHtml = htmlSanitizer.sanitize(text(draft, "bodyHtml"));
        if (bodyHtml.length() > MAX_BODY) {
            errors.add(new FieldErrorItem("bodyHtml", "正文不能超过 " + MAX_BODY + " 字符"));
        }
        if (!bodyHtml.isEmpty()) {
            normalized.put("bodyHtml", bodyHtml);
        }

        int sortOrder = 9999;
        JsonNode sortNode = draft.get("sortOrder");
        if (sortNode != null && !sortNode.isNull()) {
            if (!sortNode.canConvertToInt() || sortNode.asInt() < 0 || sortNode.asInt() > 9999) {
                errors.add(new FieldErrorItem("sortOrder", "排序值需在 0 到 9999 之间"));
            } else {
                sortOrder = sortNode.asInt();
            }
        }
        normalized.put("sortOrder", sortOrder);

        // 分类
        Long categoryId = longOrNull(draft.get("categoryId"));
        if (type.requiresCategory() && categoryId == null && forPublish) {
            errors.add(new FieldErrorItem("categoryId", "请选择分类"));
        }
        if (categoryId != null) {
            CategoryRow category = categories.findById(categoryId).orElse(null);
            if (category == null || category.categoryType() != type) {
                errors.add(new FieldErrorItem("categoryId", "分类不存在或类型不匹配"));
            } else if (forPublish && !category.status().isPublished()) {
                errors.add(new FieldErrorItem("categoryId", "分类尚未发布，请先发布分类"));
            }
            normalized.put("categoryId", categoryId);
        }

        // 封面
        Long coverId = longOrNull(draft.get("coverMediaId"));
        if (coverId != null) {
            if (!mediaExists(coverId)) {
                errors.add(new FieldErrorItem("coverMediaId", "封面图片不存在"));
            } else {
                normalized.put("coverMediaId", coverId);
            }
        }

        // 新闻展示时间
        LocalDateTime displayTime = null;
        if (type == ContentType.NEWS) {
            String raw = text(draft, "displayTime").trim();
            if (raw.isEmpty()) {
                if (forPublish) {
                    errors.add(new FieldErrorItem("displayTime", "新闻必须填写展示日期"));
                }
            } else {
                try {
                    LocalDate date = LocalDate.parse(raw.substring(0, Math.min(10, raw.length())));
                    displayTime = date.atStartOfDay();
                    normalized.put("displayTime", date.toString());
                } catch (DateTimeParseException ex) {
                    errors.add(new FieldErrorItem("displayTime", "日期格式需为 yyyy-MM-dd"));
                }
            }
        }

        // 媒体引用必须存在
        for (JsonRefs.MediaRef ref : JsonRefs.mediaRefs(draft)) {
            if (!mediaExists(ref.mediaId())) {
                errors.add(new FieldErrorItem(ref.path(), "引用的图片不存在"));
            }
        }
        // 关联内容必须存在且不能指向自己
        for (JsonRefs.ContentRef ref : JsonRefs.contentRefs(draft)) {
            if (selfId != null && ref.contentId() == selfId) {
                errors.add(new FieldErrorItem(ref.path(), "不能关联自身"));
                continue;
            }
            if (contents.findById(ref.contentId()).isEmpty()) {
                errors.add(new FieldErrorItem(ref.path(), "关联的内容不存在"));
            }
        }

        // 透传其余已定义的展示字段
        copyIfPresent(draft, normalized, "capabilities", "relatedCaseIds", "relatedProductIds",
                "relatedSolutionIds", "industry", "results", "client", "background", "actions",
                "quote", "challenge", "approach", "outcome", "seoTitle", "seoDescription");

        if (!errors.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR,
                    forPublish ? "请完善发布内容" : "请检查草稿内容", errors);
        }
        return normalized;
    }

    private void copyIfPresent(JsonNode source, ObjectNode target, String... fields) {
        for (String field : fields) {
            JsonNode value = source.get(field);
            if (value != null && !value.isNull()) {
                target.set(field, value);
            }
        }
    }

    private void replaceDraftRefs(String sourceKind, long sourceId, JsonNode draft) {
        List<MediaRefRepository.MediaRefRow> mediaRows = JsonRefs.mediaRefs(draft).stream()
                .map(ref -> new MediaRefRepository.MediaRefRow(ref.mediaId(), ref.path()))
                .toList();
        mediaRefs.replaceStage(sourceKind, sourceId, "DRAFT", mediaRows);

        List<ContentRefRepository.RefRow> contentRows = JsonRefs.contentRefs(draft).stream()
                .map(ref -> new ContentRefRepository.RefRow(ref.contentId(), ref.path()))
                .toList();
        contentRefs.replaceStage(sourceKind, sourceId, "DRAFT", contentRows);
    }

    private boolean mediaExists(long id) {
        try {
            mediaService.require(id);
            return true;
        } catch (ApiException ex) {
            return false;
        }
    }

    private LocalDateTime displayTimeOf(JsonNode normalized) {
        String raw = text(normalized, "displayTime");
        return raw.isEmpty() ? null : LocalDate.parse(raw).atStartOfDay();
    }

    private PublicationStatus parseStatus(String raw) {
        try {
            return PublicationStatus.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            throw ApiException.validation("publicationStatus", "状态不合法");
        }
    }

    private ContentView toView(ContentRow row, Map<Long, String> categoryNames) {
        String categoryName = null;
        if (row.publishedCategoryId() != null) {
            categoryName = categoryNames.containsKey(row.publishedCategoryId())
                    ? categoryNames.get(row.publishedCategoryId())
                    : categories.findById(row.publishedCategoryId())
                    .map(CategoryRow::publishedName).orElse(null);
        }
        return new ContentView(
                String.valueOf(row.id()),
                row.contentType().name(),
                row.publishedTitle() != null ? row.publishedTitle() : text(row.draft(), "title"),
                row.status().name(),
                row.draftVersion(),
                row.publishedVersion(),
                row.lockVersion(),
                row.hasUnpublishedChanges(),
                row.publishedCategoryId() == null ? null : String.valueOf(row.publishedCategoryId()),
                categoryName,
                row.publishedSortOrder() == null ? intOrDefault(row.draft().get("sortOrder"), 9999)
                        : row.publishedSortOrder(),
                row.publishedDisplayTime() == null ? null : row.publishedDisplayTime().toLocalDate().toString(),
                row.publishedAt() == null ? null : row.publishedAt().atOffset(ZoneOffset.UTC).toString()
        );
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
            return node.asLong();
        }
        String raw = node.asString().trim();
        return raw.matches("^\\d{1,18}$") ? Long.parseLong(raw) : null;
    }

    private int intOrDefault(JsonNode node, int fallback) {
        return node != null && node.canConvertToInt() ? node.asInt() : fallback;
    }

    public record ContentView(
            String id,
            String contentType,
            String title,
            String status,
            int draftVersion,
            Integer publishedVersion,
            int lockVersion,
            boolean hasUnpublishedChanges,
            String categoryId,
            String categoryName,
            int sortOrder,
            String displayTime,
            String publishedAt
    ) {
    }

    public record ContentDetailView(
            ContentView content,
            JsonNode draft,
            JsonNode published
    ) {
    }
}

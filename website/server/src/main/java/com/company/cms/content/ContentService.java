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
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    private static final int MAX_BODY = 100_000;

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
     * 草稿与发布共用一套结构校验；发布时额外要求必填项、分类已发布、关联内容已发布。
     * 字段范围按文档 6.1 通用字段与 6.5～6.9 类型专属字段收敛，未定义字段一律丢弃。
     */
    private JsonNode validate(ContentType type, JsonNode draft, boolean forPublish, Long selfId) {
        if (draft == null || !draft.isObject()) {
            throw ApiException.validation("draft", "内容必须是 JSON 对象");
        }
        List<FieldErrorItem> errors = new ArrayList<>();
        if (type == ContentType.CUSTOMER) {
            return validateCustomer(draft, forPublish, errors);
        }

        ObjectNode normalized = objectMapper.createObjectNode();

        String title = text(draft, "title").trim();
        if (title.isEmpty()) {
            if (forPublish) {
                errors.add(new FieldErrorItem("title", "发布前必须填写标题"));
            }
        } else if (title.length() > MAX_TITLE) {
            errors.add(new FieldErrorItem("title", "标题不能超过 " + MAX_TITLE + " 字"));
        }
        normalized.put("title", title);

        String summary = text(draft, "summary").trim();
        if (summary.length() > MAX_SUMMARY) {
            errors.add(new FieldErrorItem("summary", "摘要不能超过 " + MAX_SUMMARY + " 字"));
        }
        if (forPublish && summary.isEmpty() && type != ContentType.NEWS) {
            errors.add(new FieldErrorItem("summary", "发布前必须填写摘要"));
        }
        normalized.put("summary", summary);

        String bodyHtml = htmlSanitizer.sanitize(text(draft, "bodyHtml"));
        if (bodyHtml.length() > MAX_BODY) {
            errors.add(new FieldErrorItem("bodyHtml", "正文不能超过 " + MAX_BODY + " 字符"));
        }
        if (forPublish && bodyHtml.isBlank()) {
            errors.add(new FieldErrorItem("bodyHtml", "发布前必须填写正文"));
        }
        if (!bodyHtml.isEmpty()) {
            normalized.put("bodyHtml", bodyHtml);
        }

        normalized.put("sortOrder", sortOrder(draft, errors));

        // 分类：类型必须匹配；发布时案例与新闻必填
        Long categoryId = longOrNull(draft.get("categoryId"));
        if (categoryId == null) {
            if (forPublish && type.requiresCategoryOnPublish()) {
                errors.add(new FieldErrorItem("categoryId", "该类型发布前必须选择分类"));
            }
        } else {
            CategoryRow category = categories.findById(categoryId).orElse(null);
            if (category == null || category.categoryType() != type) {
                errors.add(new FieldErrorItem("categoryId", "分类不存在或类型不匹配"));
            } else {
                if (forPublish && !category.status().isPublished()) {
                    errors.add(new FieldErrorItem("categoryId", "分类尚未发布，请先发布分类"));
                }
                normalized.put("categoryId", categoryId);
            }
        }

        // 封面
        Long coverId = mediaId(draft.get("coverMediaId"), "coverMediaId", "封面图片", errors);
        if (coverId == null && forPublish) {
            errors.add(new FieldErrorItem("coverMediaId", "发布前必须选择封面"));
        }
        if (coverId != null) {
            normalized.put("coverMediaId", coverId);
            String coverAlt = limit(text(draft, "coverAlt"), 120, "coverAlt", "封面替代文字", errors);
            normalized.put("coverAlt", coverAlt.isEmpty() ? title : coverAlt);
        }

        // 可选主图：未填时前台回退封面
        Long heroId = mediaId(draft.get("heroMediaId"), "heroMediaId", "主图", errors);
        if (heroId != null) {
            normalized.put("heroMediaId", heroId);
        }

        // SEO
        JsonNode seo = draft.get("seo");
        ObjectNode seoNode = objectMapper.createObjectNode();
        String seoTitle = seo == null ? "" : limit(text(seo, "title"), 80, "seo.title", "SEO 标题", errors);
        String seoDescription = seo == null ? ""
                : limit(text(seo, "description"), 200, "seo.description", "SEO 描述", errors);
        if (!seoTitle.isEmpty()) {
            seoNode.put("title", seoTitle);
        }
        if (!seoDescription.isEmpty()) {
            seoNode.put("description", seoDescription);
        }
        if (!seoNode.isEmpty()) {
            normalized.set("seo", seoNode);
        }

        switch (type) {
            case PRODUCT -> {
                normalized.set("features", titledItems(draft.get("features"), "features", 8, true, errors));
                normalized.set("relatedCaseIds",
                        refIds(draft.get("relatedCaseIds"), "relatedCaseIds", 6,
                                ContentType.CASE, forPublish, selfId, errors));
            }
            case SOLUTION -> {
                normalized.put("industry", limit(text(draft, "industry"), 60, "industry", "所属行业", errors));
                normalized.set("pains", titledItems(draft.get("pains"), "pains", 6, false, errors));
                normalized.set("benefits", titledItems(draft.get("benefits"), "benefits", 6, false, errors));
                normalized.set("relatedProductIds",
                        refIds(draft.get("relatedProductIds"), "relatedProductIds", 6,
                                ContentType.PRODUCT, forPublish, selfId, errors));
            }
            case CASE -> {
                String customerName = limit(text(draft, "customerName"), 60,
                        "customerName", "客户名称", errors);
                if (forPublish && customerName.isBlank()) {
                    errors.add(new FieldErrorItem("customerName", "发布前必须填写客户名称"));
                }
                normalized.put("customerName", customerName);
                normalized.put("industry", limit(text(draft, "industry"), 60, "industry", "所属行业", errors));
                normalized.set("outcomes", outcomes(draft.get("outcomes"), errors));
                String quoteText = limit(text(draft, "quoteText"), 300, "quoteText", "引用语", errors);
                String quoteAuthor = limit(text(draft, "quoteAuthor"), 60, "quoteAuthor", "引用人", errors);
                if (quoteText.isEmpty() != quoteAuthor.isEmpty()) {
                    errors.add(new FieldErrorItem("quoteText", "引用语与引用人需成对填写"));
                }
                if (!quoteText.isEmpty()) {
                    normalized.put("quoteText", quoteText);
                    normalized.put("quoteAuthor", quoteAuthor);
                }
                normalized.set("relatedProductIds",
                        refIds(draft.get("relatedProductIds"), "relatedProductIds", 6,
                                ContentType.PRODUCT, forPublish, selfId, errors));
                normalized.set("relatedSolutionIds",
                        refIds(draft.get("relatedSolutionIds"), "relatedSolutionIds", 6,
                                ContentType.SOLUTION, forPublish, selfId, errors));
            }
            case NEWS -> {
                normalized.put("author", limit(text(draft, "author"), 50, "author", "作者", errors));
                String displayTime = displayTime(draft, forPublish, errors);
                if (!displayTime.isEmpty()) {
                    normalized.put("displayTime", displayTime);
                }
                String sourceName = limit(text(draft, "sourceName"), 100, "sourceName", "来源名称", errors);
                String sourceUrl = externalUrl(draft.get("sourceUrl"), "sourceUrl", "来源链接", errors);
                if (!sourceUrl.isEmpty() && sourceName.isEmpty()) {
                    errors.add(new FieldErrorItem("sourceName", "填写来源链接时必须填写来源名称"));
                }
                normalized.put("sourceName", sourceName);
                if (!sourceUrl.isEmpty()) {
                    normalized.put("sourceUrl", sourceUrl);
                }
            }
            default -> {
            }
        }

        if (!errors.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR,
                    forPublish ? "请完善发布内容" : "请检查草稿内容", errors);
        }
        return normalized;
    }

    /** 客户标识：只有名称、Logo、替代文字、客户网址与排序（文档 6.9）。 */
    private JsonNode validateCustomer(JsonNode draft, boolean forPublish, List<FieldErrorItem> errors) {
        ObjectNode normalized = objectMapper.createObjectNode();
        String title = limit(text(draft, "title"), 60, "title", "客户名称", errors);
        if (title.isEmpty() && forPublish) {
            errors.add(new FieldErrorItem("title", "发布前必须填写客户名称"));
        }
        normalized.put("title", title);

        Long logoId = mediaId(draft.get("logoMediaId"), "logoMediaId", "客户 Logo", errors);
        if (logoId == null && forPublish) {
            errors.add(new FieldErrorItem("logoMediaId", "发布前必须上传客户 Logo"));
        }
        if (logoId != null) {
            normalized.put("logoMediaId", logoId);
        }
        String logoAlt = limit(text(draft, "logoAlt"), 120, "logoAlt", "Logo 替代文字", errors);
        normalized.put("logoAlt", logoAlt.isEmpty() ? title : logoAlt);

        String website = externalUrl(draft.get("websiteUrl"), "websiteUrl", "客户网址", errors);
        if (!website.isEmpty()) {
            normalized.put("websiteUrl", website);
        }
        normalized.put("sortOrder", sortOrder(draft, errors));

        if (!errors.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR,
                    forPublish ? "请完善发布内容" : "请检查草稿内容", errors);
        }
        return normalized;
    }

    /** features / pains / benefits 的通用结构：标题、说明、可选图标。 */
    private ArrayNode titledItems(JsonNode source, String field, int max, boolean withIcon,
                                  List<FieldErrorItem> errors) {
        ArrayNode result = objectMapper.createArrayNode();
        if (source == null || source.isNull()) {
            return result;
        }
        if (!source.isArray()) {
            errors.add(new FieldErrorItem(field, "格式不正确"));
            return result;
        }
        if (source.size() > max) {
            errors.add(new FieldErrorItem(field, "最多 " + max + " 条"));
        }
        for (int i = 0; i < Math.min(source.size(), max); i++) {
            JsonNode item = source.get(i);
            String path = field + "[" + i + "]";
            if (item == null || !item.isObject()) {
                errors.add(new FieldErrorItem(path, "格式不正确"));
                continue;
            }
            ObjectNode node = objectMapper.createObjectNode();
            String itemTitle = limit(text(item, "title"), 30, path + ".title", "标题", errors);
            if (itemTitle.isEmpty()) {
                errors.add(new FieldErrorItem(path + ".title", "请填写标题"));
            }
            node.put("title", itemTitle);
            node.put("description",
                    limit(text(item, "description"), 200, path + ".description", "说明", errors));
            if (withIcon) {
                Long iconId = mediaId(item.get("iconMediaId"), path + ".iconMediaId", "图标", errors);
                if (iconId != null) {
                    node.put("iconMediaId", iconId);
                }
            }
            result.add(node);
        }
        return result;
    }

    /** 案例成果：displayValue 1～20、label 1～40，最多 4 条（文档 6.7）。 */
    private ArrayNode outcomes(JsonNode source, List<FieldErrorItem> errors) {
        ArrayNode result = objectMapper.createArrayNode();
        if (source == null || source.isNull()) {
            return result;
        }
        if (!source.isArray()) {
            errors.add(new FieldErrorItem("outcomes", "格式不正确"));
            return result;
        }
        if (source.size() > 4) {
            errors.add(new FieldErrorItem("outcomes", "成果最多 4 条"));
        }
        for (int i = 0; i < Math.min(source.size(), 4); i++) {
            JsonNode item = source.get(i);
            String path = "outcomes[" + i + "]";
            if (item == null || !item.isObject()) {
                errors.add(new FieldErrorItem(path, "格式不正确"));
                continue;
            }
            ObjectNode node = objectMapper.createObjectNode();
            String value = limit(text(item, "displayValue"), 20, path + ".displayValue", "成果数值", errors);
            String label = limit(text(item, "label"), 40, path + ".label", "成果名称", errors);
            if (value.isEmpty() || label.isEmpty()) {
                errors.add(new FieldErrorItem(path, "成果数值与名称都必须填写"));
            }
            node.put("displayValue", value);
            node.put("label", label);
            result.add(node);
        }
        return result;
    }

    /**
     * 关联内容 ID 数组。发布时目标必须已发布且类型一致，否则公开接口会出现死链接。
     */
    private ArrayNode refIds(JsonNode source, String field, int max, ContentType expected,
                             boolean forPublish, Long selfId, List<FieldErrorItem> errors) {
        ArrayNode result = objectMapper.createArrayNode();
        if (source == null || source.isNull()) {
            return result;
        }
        if (!source.isArray()) {
            errors.add(new FieldErrorItem(field, "格式不正确"));
            return result;
        }
        if (source.size() > max) {
            errors.add(new FieldErrorItem(field, "最多关联 " + max + " 项"));
        }
        List<Long> seen = new ArrayList<>();
        for (int i = 0; i < Math.min(source.size(), max); i++) {
            Long id = longOrNull(source.get(i));
            String path = field + "[" + i + "]";
            if (id == null) {
                errors.add(new FieldErrorItem(path, "内容 ID 不合法"));
                continue;
            }
            if (seen.contains(id)) {
                errors.add(new FieldErrorItem(path, "同一内容不能重复关联"));
                continue;
            }
            if (selfId != null && id.equals(selfId)) {
                errors.add(new FieldErrorItem(path, "不能关联自身"));
                continue;
            }
            seen.add(id);
            ContentRow target = contents.findById(id).orElse(null);
            if (target == null) {
                errors.add(new FieldErrorItem(path, "关联的内容不存在"));
                continue;
            }
            if (target.contentType() != expected) {
                errors.add(new FieldErrorItem(path, "关联内容类型不匹配"));
                continue;
            }
            if (forPublish && !target.status().isPublished()) {
                errors.add(new FieldErrorItem(path, "关联内容尚未发布"));
                continue;
            }
            result.add(id);
        }
        return result;
    }

    private int sortOrder(JsonNode draft, List<FieldErrorItem> errors) {
        JsonNode node = draft.get("sortOrder");
        if (node == null || node.isNull()) {
            return 100;
        }
        if (!node.canConvertToInt() || node.asInt() < 0 || node.asInt() > 9999) {
            errors.add(new FieldErrorItem("sortOrder", "排序值需在 0 到 9999 之间"));
            return 100;
        }
        return node.asInt();
    }

    private String displayTime(JsonNode draft, boolean forPublish, List<FieldErrorItem> errors) {
        String raw = text(draft, "displayTime").trim();
        if (raw.isEmpty()) {
            if (forPublish) {
                errors.add(new FieldErrorItem("displayTime", "新闻必须填写展示日期"));
            }
            return "";
        }
        try {
            LocalDate date = LocalDate.parse(raw.substring(0, Math.min(10, raw.length())));
            if (date.isAfter(LocalDate.now())) {
                errors.add(new FieldErrorItem("displayTime", "展示日期不能晚于今天，首版不支持定时发布"));
                return "";
            }
            return date.toString();
        } catch (DateTimeParseException ex) {
            errors.add(new FieldErrorItem("displayTime", "日期格式需为 yyyy-MM-dd"));
            return "";
        }
    }

    private Long mediaId(JsonNode node, String path, String label, List<FieldErrorItem> errors) {
        Long id = longOrNull(node);
        if (id == null) {
            return null;
        }
        if (!mediaExists(id)) {
            errors.add(new FieldErrorItem(path, label + "不存在"));
            return null;
        }
        return id;
    }

    private String externalUrl(JsonNode node, String path, String label, List<FieldErrorItem> errors) {
        if (node == null || node.isNull()) {
            return "";
        }
        String url = node.asString().trim();
        if (url.isEmpty()) {
            return "";
        }
        String lower = url.toLowerCase();
        if ((!lower.startsWith("http://") && !lower.startsWith("https://")) || url.length() > 500) {
            errors.add(new FieldErrorItem(path, label + "必须是 http/https 完整地址"));
            return "";
        }
        return url;
    }

    private String limit(String value, int max, String path, String label, List<FieldErrorItem> errors) {
        if (value.length() > max) {
            errors.add(new FieldErrorItem(path, label + "不能超过 " + max + " 字"));
        }
        return value;
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

    /**
     * 草稿预览视图：字段结构与公开详情一致，但取当前草稿。
     * 关联内容一律使用各自最新发布版（文档 5.4），预览页会标注该规则。
     * 图片走后台鉴权地址，避免预览依赖「已发布引用」而 404。
     */
    public Map<String, Object> previewView(long id) {
        ContentRow row = contents.findById(id)
                .orElseThrow(() -> ApiException.notFound("内容不存在"));
        JsonNode draft = row.draft() == null ? objectMapper.createObjectNode() : row.draft();

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", String.valueOf(row.id()));
        view.put("contentType", row.contentType().name());
        view.put("status", row.status().name());
        view.put("title", text(draft, "title"));
        view.put("summary", text(draft, "summary"));
        view.put("bodyHtml", text(draft, "bodyHtml"));
        view.put("cover", previewMedia(draft.get("coverMediaId")));
        view.put("logo", previewMedia(draft.get("logoMediaId")));
        view.put("hero", previewMedia(draft.get("heroMediaId")));
        view.put("categoryName", longOrNull(draft.get("categoryId")) == null ? null
                : categories.findById(longOrNull(draft.get("categoryId")))
                .map(CategoryRow::draftName).orElse(null));

        for (String field : List.of("features", "capabilities", "outcomes", "pains", "benefits",
                "quoteText", "quoteAuthor", "customerName", "industry", "displayTime", "author")) {
            JsonNode value = draft.get(field);
            if (value != null && !value.isNull()) {
                view.put(field, value);
            }
        }

        List<Map<String, Object>> related = new ArrayList<>();
        for (JsonRefs.ContentRef ref : JsonRefs.contentRefs(draft)) {
            contents.findPublishedById(ref.contentId()).ifPresent(target -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", String.valueOf(target.id()));
                item.put("contentType", target.contentType().name());
                item.put("title", text(target.published(), "title"));
                related.add(item);
            });
        }
        view.put("related", related);
        return view;
    }

    private Map<String, Object> previewMedia(JsonNode node) {
        Long mediaId = longOrNull(node);
        if (mediaId == null) {
            return null;
        }
        try {
            var media = mediaService.require(mediaId);
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("url", "/api/admin/media/" + mediaId + "/file");
            view.put("width", media.width());
            view.put("height", media.height());
            view.put("alt", "");
            return view;
        } catch (ApiException ex) {
            return null;
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
                row.publishedAt() == null ? null : row.publishedAt().atOffset(ZoneOffset.UTC).toString(),
                coverMediaId(row),
                row.updatedAt() == null ? null : row.updatedAt().atOffset(ZoneOffset.UTC).toString()
        );
    }

    /**
     * 列表缩略图：客户标识用 logo，其余用封面。优先草稿，回退发布快照，
     * 这样运营在列表里看到的就是自己正在编辑的那张图。
     */
    private String coverMediaId(ContentRow row) {
        String field = row.contentType() == ContentType.CUSTOMER ? "logoMediaId" : "coverMediaId";
        JsonNode draftValue = row.draft() == null ? null : row.draft().get(field);
        if (draftValue != null && !draftValue.isNull()) {
            return draftValue.asString();
        }
        JsonNode publishedValue = row.published() == null ? null : row.published().get(field);
        return publishedValue == null || publishedValue.isNull() ? null : publishedValue.asString();
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
            String publishedAt,
            String coverMediaId,
            String updatedAt
    ) {
    }

    public record ContentDetailView(
            ContentView content,
            JsonNode draft,
            JsonNode published
    ) {
    }
}

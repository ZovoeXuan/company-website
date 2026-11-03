package com.company.cms.content;

import com.company.cms.auth.CmsPrincipal;
import com.company.cms.common.ApiException;
import com.company.cms.common.ErrorCode;
import com.company.cms.common.FieldErrorItem;
import com.company.cms.common.PageQuery;
import com.company.cms.common.PageResult;
import com.company.cms.system.AuditLogService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类：单层、按内容类型隔离。发布时把名称与排序写入投影列，
 * 同名由唯一键 (category_type, active_published_name) 兜底。
 */
@Service
public class CategoryService {

    private final CategoryRepository repository;
    private final AuditLogService audit;
    private final ObjectMapper objectMapper;

    public CategoryService(CategoryRepository repository, AuditLogService audit, ObjectMapper objectMapper) {
        this.repository = repository;
        this.audit = audit;
        this.objectMapper = objectMapper;
    }

    public PageResult<CategoryView> list(String typeRaw, String keyword, PageQuery query) {
        ContentType type = typeRaw == null || typeRaw.isBlank() ? null : ContentType.parse(typeRaw);
        List<CategoryView> items = repository.page(type, keyword, query).items().stream()
                .map(this::toView).toList();
        long total = repository.count(type, keyword);
        return PageResult.of(items, query.page(), query.pageSize(), total);
    }

    public CategoryView detail(long id) {
        CategoryRow row = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("分类不存在"));
        return toView(row);
    }

    @Transactional
    public String create(String typeRaw, JsonNode draft, CmsPrincipal actor) {
        ContentType type = ContentType.parse(typeRaw);
        JsonNode normalized = validateAndNormalize(draft);
        checkDuplicateName(type, normalized.get("name").asString(), null);
        long id = repository.insert(type, normalized, actor.id());
        audit.success(actor.id(), "CATEGORY_CREATE", "CATEGORY", id,
                "新建分类 " + normalized.get("name").asString());
        return String.valueOf(id);
    }

    @Transactional
    public void update(long id, JsonNode draft, int expectedLockVersion, CmsPrincipal actor) {
        CategoryRow row = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("分类不存在"));
        JsonNode normalized = validateAndNormalize(draft);
        checkDuplicateName(row.categoryType(), normalized.get("name").asString(), id);
        int updated = repository.updateDraft(id, normalized, expectedLockVersion, actor.id());
        if (updated == 0) {
            throw new ApiException(ErrorCode.VERSION_CONFLICT, "分类已被他人修改，请刷新后重试");
        }
        audit.success(actor.id(), "CATEGORY_UPDATE", "CATEGORY", id,
                "修改分类 " + row.draftName() + " → " + normalized.get("name").asString());
    }

    /**
     * 同名分类在发布时会被唯一键拒绝，但那时运营已经填完整个表单。
     * 草稿阶段就提示，能直接跳到字段上改，避免发布时才发现。
     */
    private void checkDuplicateName(ContentType type, String name, Long selfId) {
        if (name == null || name.isBlank()) {
            return;
        }
        if (repository.existsDraftName(type, name.trim(), selfId)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "同类型下已存在同名分类",
                    List.of(new FieldErrorItem("name", "同类型下已存在同名分类")));
        }
    }

    @Transactional
    public void publish(long id, int expectedLockVersion, int expectedDraftVersion, CmsPrincipal actor) {
        CategoryRow row = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("分类不存在"));

        if (row.draftVersion() != expectedDraftVersion) {
            throw new ApiException(ErrorCode.VERSION_CONFLICT, "草稿已变化，请刷新后重新发布");
        }
        List<FieldErrorItem> errors = new ArrayList<>();
        if (row.draftName() == null || row.draftName().isBlank()) {
            errors.add(new FieldErrorItem("name", "请填写分类名称"));
        }
        if (!errors.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "请完善发布内容", errors);
        }

        try {
            int updated = repository.publish(id, row.draftName(), row.draftSortOrder(),
                    expectedLockVersion, expectedDraftVersion, actor.id());
            if (updated == 0) {
                throw new ApiException(ErrorCode.VERSION_CONFLICT, "分类已被他人修改，请刷新后重试");
            }
        } catch (DuplicateKeyException ex) {
            throw ApiException.validation("name", "同类型下已存在同名分类");
        }

        audit.success(actor.id(), "CATEGORY_PUBLISH", "CATEGORY", id,
                "发布分类 " + row.draftName());
    }

    @Transactional
    public void offline(long id, int expectedLockVersion, CmsPrincipal actor) {
        CategoryRow row = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("分类不存在"));
        long inUse = repository.countPublishedContents(id);
        if (inUse > 0) {
            throw new ApiException(ErrorCode.REFERENCED,
                    "该分类下仍有 " + inUse + " 条已发布内容，请先下线或调整内容分类");
        }
        int updated = repository.offline(id, expectedLockVersion, actor.id());
        if (updated == 0) {
            throw new ApiException(ErrorCode.VERSION_CONFLICT, "分类状态已变化，请刷新后重试");
        }
        audit.success(actor.id(), "CATEGORY_OFFLINE", "CATEGORY", id, "下线分类 " + row.publishedName());
    }

    @Transactional
    public void delete(long id, int expectedLockVersion, CmsPrincipal actor) {
        CategoryRow row = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("分类不存在"));
        if (row.status().isPublished()) {
            throw new ApiException(ErrorCode.REFERENCED, "已发布的分类不能直接删除，请先下线");
        }
        if (repository.countDraftReferences(id) > 0) {
            throw new ApiException(ErrorCode.REFERENCED, "该分类仍被内容引用，无法删除");
        }
        int updated = repository.logicalDelete(id, expectedLockVersion);
        if (updated == 0) {
            throw new ApiException(ErrorCode.VERSION_CONFLICT, "分类已被他人修改，请刷新后重试");
        }
        audit.success(actor.id(), "CATEGORY_DELETE", "CATEGORY", id, "删除分类 " + row.draftName());
    }

    private JsonNode validateAndNormalize(JsonNode draft) {
        if (draft == null || !draft.isObject()) {
            throw ApiException.validation("draft", "分类内容必须是对象");
        }
        JsonNode nameNode = draft.get("name");
        String name = nameNode == null || nameNode.isNull() ? "" : nameNode.asString().trim();
        if (name.isEmpty() || name.length() > 30) {
            throw ApiException.validation("name", "分类名称长度需在 1 到 30 之间");
        }
        int sortOrder = 9999;
        JsonNode sortNode = draft.get("sortOrder");
        if (sortNode != null && !sortNode.isNull()) {
            if (!sortNode.canConvertToInt() || sortNode.asInt() < 0 || sortNode.asInt() > 9999) {
                throw ApiException.validation("sortOrder", "排序值需在 0 到 9999 之间");
            }
            sortOrder = sortNode.asInt();
        }
        ObjectNode normalized = objectMapper.createObjectNode();
        normalized.put("name", name);
        normalized.put("sortOrder", sortOrder);
        return normalized;
    }

    private CategoryView toView(CategoryRow row) {
        return new CategoryView(String.valueOf(row.id()), row.categoryType().name(),
                row.draftName(), row.publishedName(), row.status().name(),
                row.draftVersion(), row.publishedVersion(), row.lockVersion(),
                row.draftSortOrder(), row.publishedSortOrder(), row.hasUnpublishedChanges());
    }

    public record CategoryView(String id, String categoryType, String name, String publishedName,
                               String status, int draftVersion, Integer publishedVersion, int lockVersion,
                               int sortOrder, Integer publishedSortOrder, boolean hasUnpublishedChanges) {
    }
}

package com.company.cms.content;

import com.company.cms.auth.CmsPrincipal;
import com.company.cms.common.ApiResponse;
import com.company.cms.common.PageQuery;
import com.company.cms.common.PageResult;
import com.company.cms.common.RequestIdFilter;
import com.company.cms.content.dto.CreateCategoryRequest;
import com.company.cms.content.dto.PublishRequest;
import com.company.cms.content.dto.UpdateCategoryRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final ObjectMapper objectMapper;

    public AdminCategoryController(CategoryService categoryService, ObjectMapper objectMapper) {
        this.categoryService = categoryService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ApiResponse<PageResult<CategoryService.CategoryView>> list(
            @RequestParam(required = false) String categoryType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            HttpServletRequest request) {
        PageQuery query = PageQuery.of(page, pageSize, 20);
        return ApiResponse.ok(categoryService.list(categoryType, keyword, query),
                RequestIdFilter.currentId(request));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> create(@Valid @RequestBody CreateCategoryRequest body,
                                                      @AuthenticationPrincipal CmsPrincipal actor,
                                                      HttpServletRequest request) {
        ObjectNode draft = objectMapper.createObjectNode();
        draft.put("name", body.name().trim());
        draft.put("sortOrder", body.sortOrder() == null ? 9999 : body.sortOrder());
        String id = categoryService.create(body.categoryType(), draft, actor);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(id, RequestIdFilter.currentId(request)));
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryService.CategoryView> detail(@PathVariable long id,
                                                            HttpServletRequest request) {
        return ApiResponse.ok(categoryService.detail(id), RequestIdFilter.currentId(request));
    }

    @PutMapping("/{id}/draft")
    public ApiResponse<Void> update(@PathVariable long id,
                                    @Valid @RequestBody UpdateCategoryRequest body,
                                    @AuthenticationPrincipal CmsPrincipal actor,
                                    HttpServletRequest request) {
        ObjectNode draft = objectMapper.createObjectNode();
        draft.put("name", body.name().trim());
        draft.put("sortOrder", body.sortOrder() == null ? 9999 : body.sortOrder());
        categoryService.update(id, draft, body.expectedLockVersion(), actor);
        return ApiResponse.ok(null, RequestIdFilter.currentId(request));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> publish(@PathVariable long id,
                                     @Valid @RequestBody PublishRequest body,
                                     @AuthenticationPrincipal CmsPrincipal actor,
                                     HttpServletRequest request) {
        categoryService.publish(id, body.expectedLockVersion(), body.expectedDraftVersion(), actor);
        return ApiResponse.ok(null, RequestIdFilter.currentId(request));
    }

    @PostMapping("/{id}/offline")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> offline(@PathVariable long id,
                                     @Valid @RequestBody PublishRequest body,
                                     @AuthenticationPrincipal CmsPrincipal actor,
                                     HttpServletRequest request) {
        categoryService.offline(id, body.expectedLockVersion(), actor);
        return ApiResponse.ok(null, RequestIdFilter.currentId(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable long id,
                                    @RequestParam int expectedLockVersion,
                                    @AuthenticationPrincipal CmsPrincipal actor,
                                    HttpServletRequest request) {
        categoryService.delete(id, expectedLockVersion, actor);
        return ApiResponse.ok(null, RequestIdFilter.currentId(request));
    }
}

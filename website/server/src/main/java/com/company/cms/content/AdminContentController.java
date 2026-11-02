package com.company.cms.content;

import com.company.cms.auth.CmsPrincipal;
import com.company.cms.common.ApiResponse;
import com.company.cms.common.PageQuery;
import com.company.cms.common.PageResult;
import com.company.cms.common.RequestIdFilter;
import com.company.cms.content.dto.CreateContentRequest;
import com.company.cms.content.dto.PublishRequest;
import com.company.cms.content.dto.SaveDraftRequest;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/contents")
public class AdminContentController {

    private final ContentService contentService;

    public AdminContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping
    public ApiResponse<PageResult<ContentService.ContentView>> list(
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String publicationStatus,
            @RequestParam(required = false) Boolean hasUnpublishedChanges,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            HttpServletRequest request) {
        PageQuery query = PageQuery.of(page, pageSize, 20);
        return ApiResponse.ok(contentService.list(contentType, keyword, categoryId,
                publicationStatus, hasUnpublishedChanges, query), RequestIdFilter.currentId(request));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContentService.ContentDetailView>> create(
            @Valid @RequestBody CreateContentRequest body,
            @AuthenticationPrincipal CmsPrincipal actor,
            HttpServletRequest request) {
        ContentService.ContentDetailView created = contentService.create(body.contentType(), body.draft(), actor);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, RequestIdFilter.currentId(request)));
    }

    @GetMapping("/{id}")
    public ApiResponse<ContentService.ContentDetailView> detail(@PathVariable long id,
                                                               HttpServletRequest request) {
        return ApiResponse.ok(contentService.detail(id), RequestIdFilter.currentId(request));
    }

    @GetMapping("/{id}/published")
    public ApiResponse<Map<String, Object>> published(@PathVariable long id, HttpServletRequest request) {
        ContentService.ContentDetailView detail = contentService.detail(id);
        if (detail.published() == null) {
            throw com.company.cms.common.ApiException.notFound("该内容还没有发布快照");
        }
        return ApiResponse.ok(Map.of(
                "id", detail.content().id(),
                "publishedVersion", detail.content().publishedVersion(),
                "published", detail.published()), RequestIdFilter.currentId(request));
    }

    @PutMapping("/{id}/draft")
    public ApiResponse<ContentService.ContentDetailView> saveDraft(
            @PathVariable long id,
            @Valid @RequestBody SaveDraftRequest body,
            @AuthenticationPrincipal CmsPrincipal actor,
            HttpServletRequest request) {
        return ApiResponse.ok(contentService.saveDraft(id, body.draft(),
                body.expectedLockVersion(), actor), RequestIdFilter.currentId(request));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> publish(@PathVariable long id,
                                     @Valid @RequestBody PublishRequest body,
                                     @AuthenticationPrincipal CmsPrincipal actor,
                                     HttpServletRequest request) {
        contentService.publish(id, body.expectedLockVersion(), body.expectedDraftVersion(), actor);
        return ApiResponse.ok(null, RequestIdFilter.currentId(request));
    }

    @PostMapping("/{id}/offline")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> offline(@PathVariable long id,
                                     @Valid @RequestBody PublishRequest body,
                                     @AuthenticationPrincipal CmsPrincipal actor,
                                     HttpServletRequest request) {
        contentService.offline(id, body.expectedLockVersion(), actor);
        return ApiResponse.ok(null, RequestIdFilter.currentId(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable long id,
                                    @RequestParam int expectedLockVersion,
                                    @AuthenticationPrincipal CmsPrincipal actor,
                                    HttpServletRequest request) {
        contentService.delete(id, expectedLockVersion, actor);
        return ApiResponse.ok(null, RequestIdFilter.currentId(request));
    }

    @GetMapping("/{id}/references")
    public ApiResponse<List<Map<String, Object>>> references(@PathVariable long id,
                                                             HttpServletRequest request) {
        return ApiResponse.ok(contentService.referenceViews(id), RequestIdFilter.currentId(request));
    }
}

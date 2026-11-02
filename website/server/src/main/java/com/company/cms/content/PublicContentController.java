package com.company.cms.content;

import com.company.cms.common.ApiResponse;
import com.company.cms.common.PageResult;
import com.company.cms.common.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 公开内容接口。只返回已发布快照，未发布或已下线一律 404。
 */
@RestController
@RequestMapping("/api/public")
public class PublicContentController {

    private final PublicContentService publicContent;

    public PublicContentController(PublicContentService publicContent) {
        this.publicContent = publicContent;
    }

    @GetMapping("/categories")
    public ApiResponse<List<Map<String, Object>>> categories(@RequestParam String type,
                                                             HttpServletRequest request) {
        return ApiResponse.ok(publicContent.categories(ContentType.parse(type)),
                RequestIdFilter.currentId(request));
    }

    @GetMapping("/products")
    public ApiResponse<PageResult<Map<String, Object>>> products(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        return list(ContentType.PRODUCT, page, pageSize, categoryId, keyword, request);
    }

    @GetMapping("/products/{id}")
    public ApiResponse<Map<String, Object>> product(@PathVariable long id, HttpServletRequest request) {
        return ApiResponse.ok(publicContent.detail(ContentType.PRODUCT, id),
                RequestIdFilter.currentId(request));
    }

    @GetMapping("/solutions")
    public ApiResponse<PageResult<Map<String, Object>>> solutions(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        return list(ContentType.SOLUTION, page, pageSize, categoryId, keyword, request);
    }

    @GetMapping("/solutions/{id}")
    public ApiResponse<Map<String, Object>> solution(@PathVariable long id, HttpServletRequest request) {
        return ApiResponse.ok(publicContent.detail(ContentType.SOLUTION, id),
                RequestIdFilter.currentId(request));
    }

    @GetMapping("/cases")
    public ApiResponse<PageResult<Map<String, Object>>> cases(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        return list(ContentType.CASE, page, pageSize, categoryId, keyword, request);
    }

    @GetMapping("/cases/{id}")
    public ApiResponse<Map<String, Object>> caseDetail(@PathVariable long id, HttpServletRequest request) {
        return ApiResponse.ok(publicContent.detail(ContentType.CASE, id),
                RequestIdFilter.currentId(request));
    }

    @GetMapping("/news")
    public ApiResponse<PageResult<Map<String, Object>>> news(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        return list(ContentType.NEWS, page, pageSize, categoryId, keyword, request);
    }

    @GetMapping("/news/{id}")
    public ApiResponse<Map<String, Object>> newsDetail(@PathVariable long id, HttpServletRequest request) {
        return ApiResponse.ok(publicContent.detail(ContentType.NEWS, id),
                RequestIdFilter.currentId(request));
    }

    private ApiResponse<PageResult<Map<String, Object>>> list(ContentType type, Integer page,
                                                              Integer pageSize, Long categoryId,
                                                              String keyword, HttpServletRequest request) {
        int p = page == null ? 1 : page;
        int size = pageSize == null ? 12 : Math.min(pageSize, 50);
        if (p < 1 || size < 1) {
            throw com.company.cms.common.ApiException.validation("page", "分页参数不合法");
        }
        return ApiResponse.ok(publicContent.list(type, categoryId, keyword, p, size),
                RequestIdFilter.currentId(request));
    }
}

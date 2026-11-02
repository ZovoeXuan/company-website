package com.company.cms.system;

import com.company.cms.auth.CmsPrincipal;
import com.company.cms.common.ApiResponse;
import com.company.cms.common.PageQuery;
import com.company.cms.common.PageResult;
import com.company.cms.common.RequestIdFilter;
import com.company.cms.system.dto.CreateUserRequest;
import com.company.cms.system.dto.ResetPasswordRequest;
import com.company.cms.system.dto.UpdateUserRequest;
import com.company.cms.system.dto.UserListItem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账号管理，仅 ADMIN 可用。Controller 只做参数接收与响应封装。
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<PageResult<UserListItem>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            HttpServletRequest request) {
        PageQuery query = PageQuery.of(page, pageSize, 20);
        return ApiResponse.ok(userService.list(keyword, query), RequestIdFilter.currentId(request));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> create(@Valid @RequestBody CreateUserRequest body,
                                                      @AuthenticationPrincipal CmsPrincipal operator,
                                                      HttpServletRequest request) {
        String id = userService.create(body, operator);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(id, RequestIdFilter.currentId(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable long id,
                                    @Valid @RequestBody UpdateUserRequest body,
                                    @AuthenticationPrincipal CmsPrincipal operator,
                                    HttpServletRequest request) {
        userService.update(id, body, operator);
        return ApiResponse.ok(null, RequestIdFilter.currentId(request));
    }

    @PostMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable long id,
                                           @Valid @RequestBody ResetPasswordRequest body,
                                           @AuthenticationPrincipal CmsPrincipal operator,
                                           HttpServletRequest request) {
        userService.resetPassword(id, body, operator);
        return ApiResponse.ok(null, RequestIdFilter.currentId(request));
    }
}

package com.company.cms.lead;

import com.company.cms.auth.CmsPrincipal;
import com.company.cms.common.ApiResponse;
import com.company.cms.common.PageQuery;
import com.company.cms.common.PageResult;
import com.company.cms.common.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

/**
 * 咨询管理只对 ADMIN 开放；EDITOR 没有任何读取权限。
 * 查看详情会写审计，列表不返回完整手机号。
 */
@RestController
@RequestMapping("/api/admin/leads")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLeadController {

    private final LeadService leadService;

    public AdminLeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            HttpServletRequest request) {
        PageQuery query = PageQuery.of(page, pageSize, 20);
        LocalDate fromDate = LeadService.parseDate(from, "from");
        LocalDate toDate = LeadService.parseDate(to, "to");
        return ApiResponse.ok(leadService.list(status, keyword, fromDate, toDate,
                query.page(), query.pageSize()), RequestIdFilter.currentId(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable long id,
                                                   @AuthenticationPrincipal CmsPrincipal actor,
                                                   HttpServletRequest request) {
        return ApiResponse.ok(leadService.detail(id, actor.id()), RequestIdFilter.currentId(request));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable long id,
                                          @RequestBody LeadStatusRequest body,
                                          @AuthenticationPrincipal CmsPrincipal actor,
                                          HttpServletRequest request) {
        leadService.updateStatus(id, body, actor.id());
        return ApiResponse.ok(null, RequestIdFilter.currentId(request));
    }
}

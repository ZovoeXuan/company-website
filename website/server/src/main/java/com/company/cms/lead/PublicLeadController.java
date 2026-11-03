package com.company.cms.lead;

import com.company.cms.common.ApiResponse;
import com.company.cms.common.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公开咨询提交。该路由已在安全配置中排除 CSRF，仅接受匿名 JSON 请求。
 * 不提供任何公开读取接口。
 */
@RestController
@RequestMapping("/api/public")
public class PublicLeadController {

    private final LeadService leadService;

    public PublicLeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @PostMapping("/leads")
    public ResponseEntity<ApiResponse<LeadSubmitResponse>> submit(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody LeadSubmitRequest body,
            HttpServletRequest request) {
        LeadSubmitResponse result = leadService.submit(body, idempotencyKey, request.getRemoteAddr());
        return ResponseEntity.status(200)
                .body(ApiResponse.ok(result, RequestIdFilter.currentId(request)));
    }
}

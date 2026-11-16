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

import java.util.Map;

/**
 * 公开咨询提交与短信验证码发送。
 */
@RestController
@RequestMapping("/api/public")
public class PublicLeadController {

    private final LeadService leadService;

    public PublicLeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    public record SmsCodeRequest(String phone) {
    }

    @PostMapping("/leads/sms-code")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendSmsCode(
            @RequestBody SmsCodeRequest body,
            HttpServletRequest request) {
        Map<String, Object> result = leadService.sendSmsCode(
                body == null ? null : body.phone(), request.getRemoteAddr());
        return ResponseEntity.status(200)
                .body(ApiResponse.ok(result, RequestIdFilter.currentId(request)));
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

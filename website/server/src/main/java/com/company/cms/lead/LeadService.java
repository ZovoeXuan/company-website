package com.company.cms.lead;

import com.company.cms.common.ApiException;
import com.company.cms.common.ErrorCode;
import com.company.cms.common.FieldErrorItem;
import com.company.cms.common.PageResult;
import com.company.cms.common.UtcTime;
import com.company.cms.content.ContentRepository;
import com.company.cms.content.ContentRow;
import com.company.cms.content.ContentType;
import com.company.cms.site.SiteConfigKey;
import com.company.cms.site.SiteConfigRepository;
import com.company.cms.site.SiteConfigRow;
import com.company.cms.system.AuditLogService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 咨询线索闭环：幂等受理、敏感字段加密、限流，以及后台查看与状态处理。
 * 公开提交失败不落库；成功记录同时承载幂等结果。
 */
@Service
public class LeadService {

    private static final Pattern PHONE = Pattern.compile("^1[3-9][0-9]{9}$");
    private static final String RECEIPT_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int PAYLOAD_VERSION = 1;
    private static final int MAX_NOTE = 2000;

    private final LeadRepository repository;
    private final LeadCrypto crypto;
    private final LeadRateLimiter rateLimiter;
    private final ContentRepository contents;
    private final SiteConfigRepository configs;
    private final AuditLogService audit;
    private final SecureRandom random = new SecureRandom();

    public LeadService(LeadRepository repository, LeadCrypto crypto, LeadRateLimiter rateLimiter,
                       ContentRepository contents, SiteConfigRepository configs, AuditLogService audit) {
        this.repository = repository;
        this.crypto = crypto;
        this.rateLimiter = rateLimiter;
        this.contents = contents;
        this.configs = configs;
        this.audit = audit;
    }

    // ------------------------------------------------------------ 公开提交

    /** 公开提交不包事务：单条 INSERT 自身原子，幂等冲突后需要用新快照回读已提交记录。 */
    public LeadSubmitResponse submit(LeadSubmitRequest request, String idempotencyKey, String remoteAddr) {
        String key = normalizeKey(idempotencyKey);
        NormalizedLead lead = normalize(request);

        byte[] payloadHash = crypto.payloadHash(lead.canonicalPayload());
        String ipDigest = crypto.digest("ip:" + (remoteAddr == null ? "unknown" : remoteAddr));

        // 通用频率限制对幂等重试同样生效
        rateLimiter.checkGeneral(ipDigest);

        Optional<LeadRepository.LeadRow> existing = repository.findByIdempotencyKey(key);
        if (existing.isPresent()) {
            return replay(existing.get(), payloadHash);
        }

        ensureFormEnabled();
        ensureConsentVersion(lead.consentVersion());
        ensureContentReference(lead);

        rateLimiter.checkNewSubmission(ipDigest, crypto.digest("phone:" + lead.phone()));

        LocalDateTime now = UtcTime.now();
        String receipt = receiptNo();
        try {
            repository.insert(receipt, key, payloadHash, PAYLOAD_VERSION,
                    lead.name(), lead.companyName(), crypto.encryptPhone(lead.phone()),
                    lead.phone().substring(lead.phone().length() - 4),
                    lead.demand(), lead.sourcePath(), lead.sourcePosition(), lead.contentType(),
                    lead.contentId(), lead.consentVersion(), now);
            return new LeadSubmitResponse(true, receipt);
        } catch (DuplicateKeyException ex) {
            // 并发写入同一幂等键：等对方提交后回读，不能只依赖提交前的查询
            for (int attempt = 0; attempt < 5; attempt++) {
                Optional<LeadRepository.LeadRow> concurrent = repository.findByIdempotencyKey(key);
                if (concurrent.isPresent()) {
                    return replay(concurrent.get(), payloadHash);
                }
                try {
                    Thread.sleep(40);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            throw new ApiException(ErrorCode.IDEMPOTENCY_CONFLICT,
                    "同一提交正在处理中，请稍后重试");
        }
    }

    private LeadSubmitResponse replay(LeadRepository.LeadRow row, byte[] payloadHash) {
        if (!java.util.Arrays.equals(row.payloadHash(), payloadHash)) {
            throw new ApiException(ErrorCode.IDEMPOTENCY_CONFLICT,
                    "同一幂等键提交的内容与首次不一致，请刷新后重新填写");
        }
        return new LeadSubmitResponse(true, row.receiptNo());
    }

    private String normalizeKey(String raw) {
        if (raw == null || raw.isBlank()) {
            throw ApiException.validation("Idempotency-Key", "缺少幂等键，请刷新页面后重试");
        }
        try {
            return UUID.fromString(raw.trim()).toString().toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException ex) {
            throw ApiException.validation("Idempotency-Key", "幂等键格式不正确");
        }
    }

    private record NormalizedLead(
            String name,
            String phone,
            String companyName,
            String demand,
            String sourcePath,
            String sourcePosition,
            String contentType,
            Long contentId,
            int consentVersion,
            String canonicalPayload
    ) {
    }

    private NormalizedLead normalize(LeadSubmitRequest request) {
        if (request == null) {
            throw ApiException.validation("body", "提交内容不能为空");
        }
        List<FieldErrorItem> errors = new ArrayList<>();

        String name = clean(request.name(), 50, "name", "称呼", errors);
        String phone = clean(request.phone(), 11, "phone", "手机号", errors);
        if (!phone.isEmpty() && !PHONE.matcher(phone).matches()) {
            errors.add(new FieldErrorItem("phone", "请填写 11 位中国大陆手机号"));
        }
        String company = clean(request.companyName(), 100, "companyName", "公司名称", errors);
        String demand = clean(request.demand(), 2000, "demand", "需求描述", errors);
        String sourcePath = clean(request.sourcePath(), 200, "sourcePath", "来源路径", errors);
        if (!sourcePath.isEmpty() && (!sourcePath.startsWith("/") || sourcePath.startsWith("//")
                || sourcePath.contains("?") || sourcePath.contains("#"))) {
            errors.add(new FieldErrorItem("sourcePath", "来源路径只能是站内相对路径"));
        }
        String sourcePosition = clean(request.sourcePosition(), 32, "sourcePosition", "来源位置", errors);
        if (!sourcePosition.isEmpty()) {
            try {
                LeadSourcePosition.parse(sourcePosition);
            } catch (ApiException ex) {
                errors.add(new FieldErrorItem("sourcePosition", "咨询入口位置不合法"));
            }
        }

        String contentType = request.contentType() == null ? ""
                : request.contentType().replace("\u001f", " ").trim();
        if (contentType.length() > 20) {
            errors.add(new FieldErrorItem("contentType", "关联类型不能超过 20 字"));
        }
        Long contentId = request.contentId();
        if ((contentType.isEmpty()) != (contentId == null)) {
            errors.add(new FieldErrorItem("contentId", "关联类型与内容 ID 需成对填写"));
        }
        if (!contentType.isEmpty()) {
            try {
                ContentType type = ContentType.parse(contentType);
                if (type != ContentType.PRODUCT && type != ContentType.SOLUTION && type != ContentType.CASE) {
                    errors.add(new FieldErrorItem("contentType", "只能关联产品、方案或案例"));
                }
            } catch (ApiException ex) {
                errors.add(new FieldErrorItem("contentType", "关联类型不合法"));
            }
        }
        if (contentId != null && contentId <= 0) {
            errors.add(new FieldErrorItem("contentId", "关联内容 ID 不合法"));
        }
        if (request.consent() == null || !request.consent()) {
            errors.add(new FieldErrorItem("consent", "请先阅读并同意隐私说明"));
        }
        Integer consentVersion = request.consentVersion();
        if (consentVersion == null || consentVersion < 1) {
            errors.add(new FieldErrorItem("consentVersion", "缺少隐私说明版本，请刷新页面后重试"));
        }

        if (!errors.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "请检查提交内容", errors);
        }

        String canonical = String.join("\u001f",
                "v" + PAYLOAD_VERSION, name, phone, company, demand, sourcePath, sourcePosition,
                contentType, contentId == null ? "" : String.valueOf(contentId),
                "true", String.valueOf(consentVersion));
        return new NormalizedLead(name, phone, company, demand, sourcePath, sourcePosition,
                contentType.isEmpty() ? null : contentType, contentId, consentVersion, canonical);
    }

    private String clean(String value, int max, String field, String label, List<FieldErrorItem> errors) {
        String text = value == null ? "" : value.replace("\u001f", " ").trim();
        if (text.isEmpty()) {
            errors.add(new FieldErrorItem(field, "请填写" + label));
        } else if (text.length() > max) {
            errors.add(new FieldErrorItem(field, label + "不能超过 " + max + " 字"));
        }
        return text;
    }

    private void ensureFormEnabled() {
        JsonNode contact = published(SiteConfigKey.CONTACT);
        boolean enabled = contact != null && contact.get("formEnabled") != null
                && contact.get("formEnabled").asBoolean(false);
        if (!enabled) {
            throw new ApiException(ErrorCode.FORM_UNAVAILABLE, "在线表单暂未启用，请通过电话或邮箱联系我们");
        }
    }

    private void ensureConsentVersion(int consentVersion) {
        SiteConfigRow privacy = configs.find(SiteConfigKey.PRIVACY).orElse(null);
        Integer current = privacy == null ? null : privacy.publishedVersion();
        if (current == null || !privacy.status().isPublished()) {
            throw new ApiException(ErrorCode.FORM_UNAVAILABLE, "隐私说明尚未发布，暂时无法提交");
        }
        if (current != consentVersion) {
            throw ApiException.validation("consentVersion", "隐私说明已更新，请重新阅读并勾选");
        }
    }

    private void ensureContentReference(NormalizedLead lead) {
        if (lead.contentType() == null || lead.contentId() == null) {
            return;
        }
        ContentRow content = contents.findPublishedById(lead.contentId()).orElse(null);
        if (content == null || !content.contentType().name().equals(lead.contentType())) {
            throw ApiException.validation("contentId", "关联内容不存在、未发布或类型不匹配");
        }
    }

    private JsonNode published(SiteConfigKey key) {
        return configs.find(key).map(SiteConfigRow::published).orElse(null);
    }

    private String receiptNo() {
        StringBuilder builder = new StringBuilder("R");
        for (int i = 0; i < 16; i++) {
            builder.append(RECEIPT_ALPHABET.charAt(random.nextInt(RECEIPT_ALPHABET.length())));
        }
        return builder.toString();
    }

    // ------------------------------------------------------------ 后台处理

    public PageResult<Map<String, Object>> list(String status, String keyword, LocalDate from,
                                                LocalDate to, int page, int pageSize) {
        String normalizedStatus = "";
        if (status != null && !status.isBlank()) {
            normalizedStatus = LeadStatus.parse(status).name();
        }
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.length() > 100) {
            throw ApiException.validation("keyword", "关键字不能超过 100 字");
        }
        int offset = (page - 1) * pageSize;
        List<LeadRepository.LeadRow> rows = repository.findPage(
                normalizedStatus, normalizedKeyword, from, to, offset, pageSize);
        long total = repository.count(normalizedStatus, normalizedKeyword, from, to);
        List<Map<String, Object>> items = rows.stream().map(this::listView).toList();
        return PageResult.of(items, page, pageSize, total);
    }

    @Transactional
    public Map<String, Object> detail(long id, long actorId) {
        LeadRepository.LeadRow row = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("咨询记录不存在"));
        audit.success(actorId, "LEAD_VIEW", "LEAD", id, "查看咨询 #" + id);
        Map<String, Object> view = detailView(row);
        view.put("phone", crypto.decryptPhone(row.phoneCiphertext()));
        return view;
    }

    @Transactional
    public void updateStatus(long id, LeadStatusRequest request, long actorId) {
        if (request == null || request.status() == null || request.status().isBlank()) {
            throw ApiException.validation("status", "请选择目标状态");
        }
        LeadStatus target = LeadStatus.parse(request.status());
        LeadRepository.LeadRow row = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("咨询记录不存在"));
        LeadStatus current = LeadStatus.parse(row.status());
        if (!current.canMoveTo(target)) {
            throw ApiException.validation("status",
                    "不能从 " + current.name() + " 变更为 " + target.name());
        }
        String note = request.note() == null ? "" : request.note().trim();
        if (note.length() > MAX_NOTE) {
            throw ApiException.validation("note", "处理备注不能超过 " + MAX_NOTE + " 字");
        }
        if (current.requiresNoteToReopen() && note.isEmpty()) {
            throw ApiException.validation("note", "重新跟进必须填写原因");
        }
        if (request.expectedLockVersion() == null) {
            throw ApiException.validation("expectedLockVersion", "缺少版本号，请刷新后重试");
        }
        int updated = repository.updateStatus(id, target.name(), note.isEmpty() ? null : note,
                actorId, request.expectedLockVersion());
        if (updated == 0) {
            throw new ApiException(ErrorCode.VERSION_CONFLICT, "线索已被他人处理，请刷新后重试");
        }
        audit.success(actorId, "LEAD_STATUS", "LEAD", id,
                "咨询 #" + id + " 状态 " + current.name() + "→" + target.name()
                        + (note.isEmpty() ? "" : "（有备注）"));
    }

    private Map<String, Object> listView(LeadRepository.LeadRow row) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", String.valueOf(row.id()));
        view.put("receiptNo", row.receiptNo());
        view.put("name", row.name());
        view.put("companyName", row.companyName());
        view.put("phoneMasked", "***" + row.phoneLast4());
        view.put("sourcePosition", row.sourcePosition());
        view.put("sourcePath", row.sourcePath());
        view.put("contentType", row.contentType());
        view.put("contentId", row.contentId() == null ? null : String.valueOf(row.contentId()));
        view.put("contentTitle", row.contentTitle());
        view.put("status", row.status());
        view.put("handledByName", row.handlerName());
        view.put("createdAt", iso(row.createdAt()));
        return view;
    }

    private Map<String, Object> detailView(LeadRepository.LeadRow row) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", String.valueOf(row.id()));
        view.put("receiptNo", row.receiptNo());
        view.put("name", row.name());
        view.put("companyName", row.companyName());
        view.put("phoneMasked", "***" + row.phoneLast4());
        view.put("demand", row.demand());
        view.put("sourcePath", row.sourcePath());
        view.put("sourcePosition", row.sourcePosition());
        view.put("contentType", row.contentType());
        view.put("contentId", row.contentId() == null ? null : String.valueOf(row.contentId()));
        view.put("contentTitle", row.contentTitle());
        view.put("consentVersion", row.consentVersion());
        view.put("consentAt", iso(row.consentAt()));
        view.put("status", row.status());
        view.put("lastNote", row.lastNote());
        view.put("handledByName", row.handlerName());
        view.put("handledAt", iso(row.handledAt()));
        view.put("lockVersion", row.lockVersion());
        view.put("createdAt", iso(row.createdAt()));
        view.put("updatedAt", iso(row.updatedAt()));
        return view;
    }

    private String iso(LocalDateTime time) {
        return time == null ? null : time.atOffset(ZoneOffset.UTC).toString();
    }

    /** 供控制器解析日期筛选；空值返回 null，格式错误转成字段级错误。 */
    public static LocalDate parseDate(String value, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (RuntimeException ex) {
            throw ApiException.validation(field, "日期格式需为 yyyy-MM-dd");
        }
    }
}

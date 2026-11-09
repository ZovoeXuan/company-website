package com.company.cms.xiaoqi;

import com.company.cms.common.ApiException;
import com.company.cms.common.ErrorCode;
import com.company.cms.common.FieldErrorItem;
import com.company.cms.common.RateLimitedException;
import com.company.cms.lead.LeadCrypto;
import com.company.cms.system.AuditLogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 小鑫 AI：服务端代理调用 OpenAI chat 兼容接口，密钥只在服务端使用。
 */
@Service
public class XiaoqiService {

    static final int MAX_QUESTION = 500;
    static final int MAX_HISTORY = 6;
    private static final Pattern HEADER_NAME = Pattern.compile("^[A-Za-z0-9-]{1,64}$");

    private final XiaoqiSecretRepository repository;
    private final LeadCrypto crypto;
    private final AuditLogService audit;
    private final ObjectMapper objectMapper;
    private final HttpClient http;
    private final int perMinute;
    private final boolean allowLocalHttp;
    private final Map<String, Deque<Instant>> hits = new ConcurrentHashMap<>();

    public XiaoqiService(XiaoqiSecretRepository repository, LeadCrypto crypto, AuditLogService audit,
                         ObjectMapper objectMapper,
                         @Value("${cms.xiaoqi.rate-per-minute:10}") int perMinute,
                         @Value("${cms.xiaoqi.allow-local-http:false}") boolean allowLocalHttp) {
        this.repository = repository;
        this.crypto = crypto;
        this.audit = audit;
        this.objectMapper = objectMapper;
        this.perMinute = perMinute;
        this.allowLocalHttp = allowLocalHttp;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    // ------------------------------------------------ public chat

    public String chat(String question, List<ChatTurn> history, String remoteAddr) {
        String q = question == null ? "" : question.trim();
        if (q.isEmpty()) {
            throw ApiException.validation("question", "请输入问题");
        }
        if (q.length() > MAX_QUESTION) {
            throw ApiException.validation("question", "问题最多 " + MAX_QUESTION + " 字");
        }
        checkRate(crypto.digest("xiaoqi|" + (remoteAddr == null ? "unknown" : remoteAddr)));
        XiaoqiSecret secret = repository.load();
        if (!secret.configured()) {
            throw new ApiException(ErrorCode.XIAOQI_UNAVAILABLE);
        }
        List<ChatTurn> turns = history == null ? List.of()
                : history.subList(Math.max(0, history.size() - MAX_HISTORY * 2), history.size());
        return call(secret, q, turns);
    }

    String call(XiaoqiSecret s, String question, List<ChatTurn> history) {
        ObjectNode body = objectMapper.createObjectNode();
        if (!s.modelName().isBlank()) {
            body.put("model", s.modelName());
        }
        ArrayNode messages = body.putArray("messages");
        if (!s.systemPrompt().isBlank()) {
            messages.addObject().put("role", "system").put("content", s.systemPrompt());
        }
        for (ChatTurn t : history) {
            if (t == null || t.content() == null) continue;
            String role = "assistant".equals(t.role()) ? "assistant" : "user";
            String content = t.content().length() > 2000 ? t.content().substring(0, 2000) : t.content();
            messages.addObject().put("role", role).put("content", content);
        }
        messages.addObject().put("role", "user").put("content", question);

        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(s.apiUrl()))
                .timeout(Duration.ofMillis(s.timeoutMs()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
        if (!s.apiKey().isEmpty()) {
            req.header(s.authHeader(), s.authPrefix() + s.apiKey());
        }
        try {
            HttpResponse<String> resp = http.send(req.build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                throw upstream("上游返回 HTTP " + resp.statusCode());
            }
            JsonNode json = objectMapper.readTree(resp.body());
            JsonNode content = json.path("choices").path(0).path("message").path("content");
            if (!content.isString() || content.asString().isBlank()) {
                throw upstream("上游响应缺少 choices[0].message.content");
            }
            return content.asString();
        } catch (ApiException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw upstream("请求被中断");
        } catch (Exception ex) {
            throw upstream(ex instanceof java.net.http.HttpTimeoutException ? "请求超时" : "无法连接上游接口");
        }
    }

    private ApiException upstream(String detail) {
        return new ApiException(ErrorCode.XIAOQI_UPSTREAM_ERROR, "智能助手暂时无法回答：" + detail);
    }

    private void checkRate(String key) {
        Instant now = Instant.now();
        Deque<Instant> q = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && q.peekFirst().isBefore(now.minusSeconds(60))) q.removeFirst();
            if (q.size() >= perMinute) {
                throw new RateLimitedException(Duration.between(now, q.peekFirst().plusSeconds(60)).toSeconds());
            }
            q.addLast(now);
        }
        if (hits.size() > 1000) {
            hits.entrySet().removeIf(e -> {
                synchronized (e.getValue()) {
                    return e.getValue().isEmpty() || e.getValue().peekLast().isBefore(now.minusSeconds(60));
                }
            });
        }
    }

    // ------------------------------------------------ admin

    public Map<String, Object> adminView() {
        XiaoqiSecret s = repository.load();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("apiUrl", s.apiUrl());
        m.put("apiKeySet", !s.apiKey().isEmpty());
        m.put("apiKeyLast4", s.apiKeyLast4());
        m.put("authHeader", s.authHeader());
        m.put("authPrefix", s.authPrefix());
        m.put("modelName", s.modelName());
        m.put("systemPrompt", s.systemPrompt());
        m.put("timeoutMs", s.timeoutMs());
        return m;
    }

    @Transactional
    public void save(IntegrationRequest r, Long actorId) {
        XiaoqiSecret merged = validate(r, repository.load());
        repository.save(merged, actorId);
        audit.success(actorId, "XIAOQI_INTEGRATION_UPDATE", "XIAOQI", 1L,
                "更新小鑫接入配置 url=" + merged.apiUrl() + " model=" + merged.modelName()
                        + " key=" + (merged.apiKey().isEmpty() ? "未设置" : "已设置"));
    }

    public Map<String, Object> test(Long actorId) {
        XiaoqiSecret s = repository.load();
        if (!s.configured()) {
            throw new ApiException(ErrorCode.XIAOQI_UNAVAILABLE, "请先保存 API URL");
        }
        long start = System.nanoTime();
        Map<String, Object> m = new LinkedHashMap<>();
        try {
            String reply = call(s, "你好，请用一句话介绍你自己。", List.of());
            m.put("ok", true);
            m.put("reply", reply.length() > 200 ? reply.substring(0, 200) + "…" : reply);
        } catch (ApiException ex) {
            m.put("ok", false);
            m.put("error", ex.getMessage());
        }
        m.put("elapsedMs", (System.nanoTime() - start) / 1_000_000);
        return m;
    }

    XiaoqiSecret validate(IntegrationRequest r, XiaoqiSecret current) {
        List<FieldErrorItem> errors = new ArrayList<>();
        String url = trim(r.apiUrl());
        if (!url.isEmpty() && !urlAllowed(url)) {
            errors.add(new FieldErrorItem("apiUrl", allowLocalHttp
                    ? "必须是 https 地址（开发环境允许 http://localhost、127.0.0.1）" : "必须是 https 地址"));
        }
        if (url.length() > 500) errors.add(new FieldErrorItem("apiUrl", "最多 500 字符"));
        String header = trim(r.authHeader()).isEmpty() ? "Authorization" : trim(r.authHeader());
        if (!HEADER_NAME.matcher(header).matches()) {
            errors.add(new FieldErrorItem("authHeader", "只能包含字母、数字和连字符"));
        }
        String prefix = r.authPrefix() == null ? "Bearer " : r.authPrefix();
        if (prefix.length() > 32 || prefix.chars().anyMatch(c -> c < 0x20)) {
            errors.add(new FieldErrorItem("authPrefix", "最多 32 个可见字符"));
        }
        String model = trim(r.modelName());
        if (model.length() > 100) errors.add(new FieldErrorItem("modelName", "最多 100 字符"));
        String prompt = r.systemPrompt() == null ? "" : r.systemPrompt().trim();
        if (prompt.length() > 4000) errors.add(new FieldErrorItem("systemPrompt", "最多 4000 字"));
        int timeout = r.timeoutMs() == null ? 15000 : r.timeoutMs();
        if (timeout < 1000 || timeout > 60000) errors.add(new FieldErrorItem("timeoutMs", "范围 1000–60000 毫秒"));
        String key;
        if (Boolean.TRUE.equals(r.clearApiKey())) {
            key = "";
        } else if (r.apiKey() != null && !r.apiKey().isBlank()) {
            key = r.apiKey().trim();
            if (key.length() > 500 || key.chars().anyMatch(c -> c < 0x20)) {
                errors.add(new FieldErrorItem("apiKey", "密钥格式不正确"));
            }
        } else {
            key = current.apiKey();
        }
        if (!errors.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "请检查提交内容", errors);
        }
        return new XiaoqiSecret(url, key, XiaoqiSecretRepository.last4(key), header, prefix, model, prompt, timeout);
    }

    private boolean urlAllowed(String url) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException ex) {
            return false;
        }
        if (uri.getHost() == null) return false;
        if ("https".equalsIgnoreCase(uri.getScheme())) return true;
        return allowLocalHttp && "http".equalsIgnoreCase(uri.getScheme())
                && ("localhost".equals(uri.getHost()) || "127.0.0.1".equals(uri.getHost()));
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    public record ChatTurn(String role, String content) { }

    public record ChatRequest(String question, List<ChatTurn> history) { }

    public record IntegrationRequest(String apiUrl, String apiKey, Boolean clearApiKey, String authHeader,
                                     String authPrefix, String modelName, String systemPrompt,
                                     Integer timeoutMs) { }
}

package com.company.cms.lead;

import com.company.cms.common.RateLimitedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 咨询提交限流：进程内滑动窗口，只保存 HMAC 摘要，不保存原始 IP 或手机号。
 * 首版单实例部署；多实例时需要换成共享存储。
 */
@Component
public class LeadRateLimiter {

    private final Map<String, Deque<Instant>> hits = new ConcurrentHashMap<>();

    private final int generalPerMinute;
    private final int ipPerMinute;
    private final int phonePerTenMinutes;

    public LeadRateLimiter(
            @Value("${cms.lead.rate.general-per-minute:20}") int generalPerMinute,
            @Value("${cms.lead.rate.ip-per-minute:3}") int ipPerMinute,
            @Value("${cms.lead.rate.phone-per-10-minutes:3}") int phonePerTenMinutes) {
        this.generalPerMinute = generalPerMinute;
        this.ipPerMinute = ipPerMinute;
        this.phonePerTenMinutes = phonePerTenMinutes;
    }

    /** 所有提交（含幂等重试）都受通用请求频率限制。 */
    public void checkGeneral(String ipDigest) {
        Optional<Duration> retry = hit("general|" + ipDigest, generalPerMinute, Duration.ofMinutes(1));
        if (retry.isPresent()) {
            throw new RateLimitedException(retry.get().toSeconds());
        }
    }

    /** 新提交额外受 IP 与手机号摘要限制；幂等重放不消耗该额度。 */
    public void checkNewSubmission(String ipDigest, String phoneDigest) {
        Optional<Duration> retry = hit("ip|" + ipDigest, ipPerMinute, Duration.ofMinutes(1));
        if (retry.isEmpty()) {
            retry = hit("phone|" + phoneDigest, phonePerTenMinutes, Duration.ofMinutes(10));
        }
        if (retry.isPresent()) {
            throw new RateLimitedException(retry.get().toSeconds());
        }
    }

    private Optional<Duration> hit(String key, int max, Duration window) {
        Instant now = Instant.now();
        Deque<Instant> queue = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (queue) {
            while (!queue.isEmpty() && queue.peekFirst().isBefore(now.minus(window))) {
                queue.removeFirst();
            }
            if (queue.size() >= max) {
                Duration wait = Duration.between(now, queue.peekFirst().plus(window));
                return Optional.of(wait.isNegative() ? Duration.ofSeconds(1) : wait);
            }
            queue.addLast(now);
            evict(now);
            return Optional.empty();
        }
    }

    private void evict(Instant now) {
        if (hits.size() < 1000) {
            return;
        }
        hits.entrySet().removeIf(entry -> {
            synchronized (entry.getValue()) {
                return entry.getValue().isEmpty()
                        || entry.getValue().peekLast().isBefore(now.minus(Duration.ofMinutes(10)));
            }
        });
    }
}

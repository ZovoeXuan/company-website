package com.company.cms.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录失败限流：同一「账号 + 来源地址」在窗口内失败达到阈值后锁定一段时间。
 * 首版单实例部署，使用进程内存储；不持久化原始 IP，也不写日志。
 * 多实例部署时需要替换为共享存储（见 schema-notes 第 5 节）。
 */
@Component
public class LoginRateLimiter {

    private record State(int failures, Instant windowStart, Instant lockedUntil) {
    }

    private final Map<String, State> states = new ConcurrentHashMap<>();

    private final int maxFailures;
    private final Duration window;
    private final Duration lockout;

    public LoginRateLimiter(
            @Value("${cms.security.login.max-failures:5}") int maxFailures,
            @Value("${cms.security.login.window-minutes:5}") long windowMinutes,
            @Value("${cms.security.login.lock-minutes:10}") long lockMinutes) {
        this.maxFailures = maxFailures;
        this.window = Duration.ofMinutes(windowMinutes);
        this.lockout = Duration.ofMinutes(lockMinutes);
    }

    /**
     * 限流键：账号（去首尾空格、统一小写）与来源地址组合，只存在内存中，不落库不写日志。
     */
    public static String key(String username, String remoteAddr) {
        String user = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        return user + "|" + (remoteAddr == null ? "unknown" : remoteAddr);
    }

    public Optional<Duration> lockedFor(String key) {
        State state = states.get(key);
        if (state == null || state.lockedUntil() == null) {
            return Optional.empty();
        }
        Instant now = Instant.now();
        if (state.lockedUntil().isAfter(now)) {
            return Optional.of(Duration.between(now, state.lockedUntil()));
        }
        states.remove(key);
        return Optional.empty();
    }

    public void onFailure(String key) {
        Instant now = Instant.now();
        states.compute(key, (k, current) -> {
            if (current == null || current.windowStart().plus(window).isBefore(now)) {
                return new State(1, now, null);
            }
            int failures = current.failures() + 1;
            if (failures >= maxFailures) {
                return new State(failures, current.windowStart(), now.plus(lockout));
            }
            return new State(failures, current.windowStart(), null);
        });
        evictExpired(now);
    }

    public void onSuccess(String key) {
        states.remove(key);
    }

    private void evictExpired(Instant now) {
        if (states.size() < 500) {
            return;
        }
        states.entrySet().removeIf(entry -> {
            State state = entry.getValue();
            Instant deadline = state.lockedUntil() != null
                    ? state.lockedUntil()
                    : state.windowStart().plus(window);
            return deadline.isBefore(now);
        });
    }
}

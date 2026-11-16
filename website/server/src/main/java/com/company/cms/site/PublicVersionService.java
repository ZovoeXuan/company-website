package com.company.cms.site;

import com.company.cms.common.ApiException;
import com.company.cms.content.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 公开版本聚合服务：为前端提供轻量实时同步版本号与 ETag/304 支持。
 * - siteConfigVersion 覆盖 SITE / NAVIGATION / CONTACT / PRIVACY
 * - homeVersion 覆盖 HOME / CONTACT
 * - aboutVersion 覆盖 ABOUT
 * - contentVersion 覆盖 cms_content 与 cms_category 已发布且未删除项的 (COUNT, SUM(published_version))
 */
@Service
public class PublicVersionService {

    private static final long CACHE_TTL_NANOS = 5_000_000_000L;

    interface DataLoader {
        RawSnapshot load();
    }

    record ConfigMeta(String status, Integer publishedVersion, LocalDateTime publishedAt) {
        int effectiveVersion() {
            return "PUBLISHED".equals(status) && publishedVersion != null ? publishedVersion : 0;
        }
    }

    record TypeStats(long count, long versionSum, LocalDateTime maxPublishedAt) {
        static final TypeStats EMPTY = new TypeStats(0L, 0L, null);
    }

    record RawSnapshot(
            Map<SiteConfigKey, ConfigMeta> configs,
            Map<String, TypeStats> contentStats,
            Map<String, TypeStats> categoryStats) {
    }

    public record VersionSnapshot(
            String siteConfigVersion,
            String homeVersion,
            String aboutVersion,
            String contentVersion,
            String publishedAt,
            String etag) {

        public Map<String, Object> toPayload() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("siteConfigVersion", siteConfigVersion);
            map.put("homeVersion", homeVersion);
            map.put("aboutVersion", aboutVersion);
            map.put("contentVersion", contentVersion);
            map.put("publishedAt", publishedAt);
            return map;
        }
    }

    private record CachedEntry(VersionSnapshot snapshot, long expiresAtNanos) {
    }

    private final DataLoader loader;
    private volatile CachedEntry cached;

    @Autowired
    public PublicVersionService(JdbcTemplate jdbc) {
        this(() -> queryFromDb(jdbc));
    }

    PublicVersionService(DataLoader loader) {
        this.loader = loader;
    }

    public VersionSnapshot currentVersion() {
        CachedEntry entry = cached;
        long now = System.nanoTime();
        if (entry != null && now < entry.expiresAtNanos()) {
            return entry.snapshot();
        }
        synchronized (this) {
            entry = cached;
            now = System.nanoTime();
            if (entry != null && now < entry.expiresAtNanos()) {
                return entry.snapshot();
            }
            VersionSnapshot fresh = compute(loader.load());
            cached = new CachedEntry(fresh, now + CACHE_TTL_NANOS);
            return fresh;
        }
    }

    /** 检查 ETag 是否命中，兼容弱 ETag 与空白。 */
    public boolean matchesIfNoneMatch(String ifNoneMatch, String currentEtag) {
        if (ifNoneMatch == null || ifNoneMatch.isBlank() || currentEtag == null) {
            return false;
        }
        String normalizedCurrent = stripWeakAndQuotes(currentEtag);
        for (String token : ifNoneMatch.split(",")) {
            String candidate = token.trim();
            if ("*".equals(candidate)) {
                return true;
            }
            if (normalizedCurrent.equals(stripWeakAndQuotes(candidate))) {
                return true;
            }
        }
        return false;
    }

    public void evictCache() {
        cached = null;
    }

    /** 事务提交后再清缓存，避免并发请求读到提交前快照并缓存 5 秒。 */
    public void evictAfterCommitOrNow() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    evictCache();
                }
            });
        } else {
            evictCache();
        }
    }

    static String computeSiteConfigVersion(Map<SiteConfigKey, ConfigMeta> configs) {
        return "SITE:" + versionOf(configs, SiteConfigKey.SITE)
                + "|NAVIGATION:" + versionOf(configs, SiteConfigKey.NAVIGATION)
                + "|CONTACT:" + versionOf(configs, SiteConfigKey.CONTACT)
                + "|PRIVACY:" + versionOf(configs, SiteConfigKey.PRIVACY);
    }

    private VersionSnapshot compute(RawSnapshot raw) {
        ConfigMeta site = raw.configs().get(SiteConfigKey.SITE);
        if (site == null || !"PUBLISHED".equals(site.status()) || site.publishedVersion() == null) {
            throw ApiException.notFound("站点尚未发布");
        }

        String siteConfigVersion = computeSiteConfigVersion(raw.configs());
        String homeVersion = "HOME:" + versionOf(raw.configs(), SiteConfigKey.HOME)
                + "|CONTACT:" + versionOf(raw.configs(), SiteConfigKey.CONTACT);
        String aboutVersion = "ABOUT:" + versionOf(raw.configs(), SiteConfigKey.ABOUT);
        String contentVersion = computeContentVersion(raw.contentStats(), raw.categoryStats());

        LocalDateTime maxPublished = null;
        for (ConfigMeta meta : raw.configs().values()) {
            if ("PUBLISHED".equals(meta.status())) {
                maxPublished = maxTime(maxPublished, meta.publishedAt());
            }
        }
        for (TypeStats stats : raw.contentStats().values()) {
            maxPublished = maxTime(maxPublished, stats.maxPublishedAt());
        }
        for (TypeStats stats : raw.categoryStats().values()) {
            maxPublished = maxTime(maxPublished, stats.maxPublishedAt());
        }

        String publishedAtStr = maxPublished == null ? null : maxPublished.atOffset(ZoneOffset.UTC).toString();
        String etag = shortEtag(siteConfigVersion + "\n" + homeVersion + "\n" + aboutVersion + "\n" + contentVersion);
        return new VersionSnapshot(siteConfigVersion, homeVersion, aboutVersion, contentVersion, publishedAtStr, etag);
    }

    private static int versionOf(Map<SiteConfigKey, ConfigMeta> configs, SiteConfigKey key) {
        ConfigMeta meta = configs.get(key);
        return meta == null ? 0 : meta.effectiveVersion();
    }

    private static String computeContentVersion(Map<String, TypeStats> contentStats, Map<String, TypeStats> categoryStats) {
        StringBuilder sb = new StringBuilder();
        for (ContentType type : ContentType.values()) {
            if (!sb.isEmpty()) sb.append('|');
            TypeStats stats = contentStats.getOrDefault(type.name(), TypeStats.EMPTY);
            sb.append(type.name()).append(':').append(stats.count()).append(',').append(stats.versionSum());
        }
        for (ContentType type : ContentType.values()) {
            if (type == ContentType.CUSTOMER) continue;
            TypeStats stats = categoryStats.getOrDefault(type.name(), TypeStats.EMPTY);
            sb.append("|CAT_").append(type.name()).append(':').append(stats.count()).append(',').append(stats.versionSum());
        }
        return sb.toString();
    }

    private static LocalDateTime maxTime(LocalDateTime a, LocalDateTime b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isAfter(b) ? a : b;
    }

    private static String stripWeakAndQuotes(String raw) {
        String s = raw.trim();
        if (s.startsWith("W/") || s.startsWith("w/")) {
            s = s.substring(2).trim();
        }
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static String shortEtag(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            String hex = HexFormat.of().formatHex(digest, 0, 8);
            return "\"" + hex + "\"";
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private static RawSnapshot queryFromDb(JdbcTemplate jdbc) {
        Map<SiteConfigKey, ConfigMeta> configs = new EnumMap<>(SiteConfigKey.class);
        jdbc.query(
                "SELECT config_key, publication_status, published_version, published_at FROM cms_config WHERE deleted = 0",
                rs -> {
                    SiteConfigKey key = SiteConfigKey.parse(rs.getString("config_key"));
                    Object verObj = rs.getObject("published_version");
                    Integer ver = verObj == null ? null : ((Number) verObj).intValue();
                    configs.put(key, new ConfigMeta(
                            rs.getString("publication_status"),
                            ver,
                            rs.getObject("published_at", LocalDateTime.class)
                    ));
                }
        );

        Map<String, TypeStats> contentStats = new HashMap<>();
        jdbc.query(
                """
                SELECT content_type, COUNT(*) AS cnt, COALESCE(SUM(published_version), 0) AS sum_ver,
                       MAX(published_at) AS max_pub
                  FROM cms_content
                 WHERE publication_status = 'PUBLISHED' AND deleted = 0
                 GROUP BY content_type
                """,
                rs -> {
                    contentStats.put(
                            rs.getString("content_type"),
                            new TypeStats(
                                    rs.getLong("cnt"),
                                    rs.getLong("sum_ver"),
                                    rs.getObject("max_pub", LocalDateTime.class)
                            )
                    );
                }
        );

        Map<String, TypeStats> categoryStats = new HashMap<>();
        jdbc.query(
                """
                SELECT category_type, COUNT(*) AS cnt, COALESCE(SUM(published_version), 0) AS sum_ver,
                       MAX(published_at) AS max_pub
                  FROM cms_category
                 WHERE publication_status = 'PUBLISHED' AND deleted = 0
                 GROUP BY category_type
                """,
                rs -> {
                    categoryStats.put(
                            rs.getString("category_type"),
                            new TypeStats(
                                    rs.getLong("cnt"),
                                    rs.getLong("sum_ver"),
                                    rs.getObject("max_pub", LocalDateTime.class)
                            )
                    );
                }
        );

        return new RawSnapshot(configs, contentStats, categoryStats);
    }
}

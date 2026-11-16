package com.company.cms.site;

import com.company.cms.common.ApiException;
import com.company.cms.common.ErrorCode;
import com.company.cms.content.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicVersionServiceTest {

    @Test
    void throwsNotFoundWhenSiteIsUnpublished() {
        Map<SiteConfigKey, PublicVersionService.ConfigMeta> configs = new EnumMap<>(SiteConfigKey.class);
        configs.put(SiteConfigKey.SITE, new PublicVersionService.ConfigMeta("UNPUBLISHED", null, null));

        PublicVersionService service = new PublicVersionService(
                () -> new PublicVersionService.RawSnapshot(configs, Map.of(), Map.of()));

        ApiException ex = assertThrows(ApiException.class, service::currentVersion);
        assertEquals(ErrorCode.NOT_FOUND, ex.code());
    }

    @Test
    void versionsChangeIndependentlyAndOfflineUpdatesContentVersion() {
        LocalDateTime t1 = LocalDateTime.of(2026, 11, 12, 8, 0, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 11, 12, 9, 0, 0);

        Map<SiteConfigKey, PublicVersionService.ConfigMeta> configs = new EnumMap<>(SiteConfigKey.class);
        configs.put(SiteConfigKey.SITE, new PublicVersionService.ConfigMeta("PUBLISHED", 2, t1));
        configs.put(SiteConfigKey.NAVIGATION, new PublicVersionService.ConfigMeta("PUBLISHED", 1, t1));
        configs.put(SiteConfigKey.CONTACT, new PublicVersionService.ConfigMeta("PUBLISHED", 1, t1));
        configs.put(SiteConfigKey.PRIVACY, new PublicVersionService.ConfigMeta("UNPUBLISHED", null, null));
        configs.put(SiteConfigKey.HOME, new PublicVersionService.ConfigMeta("PUBLISHED", 3, t1));
        configs.put(SiteConfigKey.ABOUT, new PublicVersionService.ConfigMeta("PUBLISHED", 4, t1));

        Map<String, PublicVersionService.TypeStats> content = new HashMap<>();
        content.put(ContentType.NEWS.name(), new PublicVersionService.TypeStats(4, 10, t1));

        AtomicReference<PublicVersionService.RawSnapshot> ref = new AtomicReference<>(
                new PublicVersionService.RawSnapshot(configs, content, Map.of()));
        AtomicInteger loads = new AtomicInteger();

        PublicVersionService service = new PublicVersionService(() -> {
            loads.incrementAndGet();
            return ref.get();
        });

        PublicVersionService.VersionSnapshot first = service.currentVersion();
        assertEquals("SITE:2|NAVIGATION:1|CONTACT:1|PRIVACY:0", first.siteConfigVersion());
        assertEquals("HOME:3|CONTACT:1", first.homeVersion());
        assertEquals("ABOUT:4", first.aboutVersion());
        assertTrue(first.contentVersion().contains("NEWS:4,10"));

        // 5秒内重复读取走缓存，不重复查询数据源
        assertEquals(first.etag(), service.currentVersion().etag());
        assertEquals(1, loads.get());

        // 模拟下线 1 篇新闻：count 从 4 降为 3，published_version 总和从 10 降为 7，必须立刻变化
        Map<String, PublicVersionService.TypeStats> afterOffline = new HashMap<>();
        afterOffline.put(ContentType.NEWS.name(), new PublicVersionService.TypeStats(3, 7, t2));
        ref.set(new PublicVersionService.RawSnapshot(configs, afterOffline, Map.of()));
        service.evictCache();

        PublicVersionService.VersionSnapshot second = service.currentVersion();
        assertEquals(first.siteConfigVersion(), second.siteConfigVersion());
        assertEquals(first.homeVersion(), second.homeVersion());
        assertEquals(first.aboutVersion(), second.aboutVersion());
        assertNotEquals(first.contentVersion(), second.contentVersion());
        assertNotEquals(first.etag(), second.etag());
        assertTrue(second.contentVersion().contains("NEWS:3,7"));

        // 模拟发布 PRIVACY：仅 siteConfigVersion 变化，home/about/content 不串扰
        Map<SiteConfigKey, PublicVersionService.ConfigMeta> configsAfterPrivacy = new EnumMap<>(configs);
        configsAfterPrivacy.put(SiteConfigKey.PRIVACY, new PublicVersionService.ConfigMeta("PUBLISHED", 1, t2));
        ref.set(new PublicVersionService.RawSnapshot(configsAfterPrivacy, afterOffline, Map.of()));
        service.evictCache();

        PublicVersionService.VersionSnapshot third = service.currentVersion();
        assertNotEquals(second.siteConfigVersion(), third.siteConfigVersion());
        assertEquals(second.homeVersion(), third.homeVersion());
        assertEquals(second.aboutVersion(), third.aboutVersion());
        assertEquals(second.contentVersion(), third.contentVersion());
    }

    @Test
    void controllerReturns304WithEmptyBodyOnMatchingEtag() {
        Map<SiteConfigKey, PublicVersionService.ConfigMeta> configs = new EnumMap<>(SiteConfigKey.class);
        configs.put(SiteConfigKey.SITE, new PublicVersionService.ConfigMeta("PUBLISHED", 1, LocalDateTime.now()));

        PublicVersionService service = new PublicVersionService(
                () -> new PublicVersionService.RawSnapshot(configs, Map.of(), Map.of()));
        PublicSiteConfigController controller = new PublicSiteConfigController(null, null, service);

        MockHttpServletRequest req = new MockHttpServletRequest();
        ResponseEntity<?> first = controller.version(null, req);
        assertEquals(200, first.getStatusCode().value());
        String etag = first.getHeaders().getETag();
        assertFalse(etag == null || etag.isBlank());
        assertTrue(first.getHeaders().getCacheControl() != null && first.getHeaders().getCacheControl().contains("no-store"));

        ResponseEntity<?> notModified = controller.version(etag, req);
        assertEquals(304, notModified.getStatusCode().value());
        assertNull(notModified.getBody());
        assertEquals(etag, notModified.getHeaders().getETag());
    }

    @Test
    void doesNotCollideWhenReplacementKeepsSameCountAndVersionSum() {
        LocalDateTime t1 = LocalDateTime.of(2026, 11, 12, 8, 0, 0);
        Map<SiteConfigKey, PublicVersionService.ConfigMeta> configs = new EnumMap<>(SiteConfigKey.class);
        configs.put(SiteConfigKey.SITE, new PublicVersionService.ConfigMeta("PUBLISHED", 1, t1));

        Map<String, PublicVersionService.TypeStats> before = new HashMap<>();
        before.put(ContentType.PRODUCT.name(), new PublicVersionService.TypeStats(2, 4, t1, "1:2,2:2"));

        Map<String, PublicVersionService.TypeStats> after = new HashMap<>();
        after.put(ContentType.PRODUCT.name(), new PublicVersionService.TypeStats(2, 4, t1, "1:1,3:3"));

        AtomicReference<PublicVersionService.RawSnapshot> ref = new AtomicReference<>(
                new PublicVersionService.RawSnapshot(configs, before, Map.of()));
        PublicVersionService service = new PublicVersionService(ref::get);

        PublicVersionService.VersionSnapshot s1 = service.currentVersion();
        ref.set(new PublicVersionService.RawSnapshot(configs, after, Map.of()));
        service.evictCache();
        PublicVersionService.VersionSnapshot s2 = service.currentVersion();

        assertNotEquals(s1.contentVersion(), s2.contentVersion());
        assertNotEquals(s1.etag(), s2.etag());
    }
}

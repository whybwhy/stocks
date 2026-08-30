package com.example.stocks.supabase;

import com.example.stocks.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 공개 화면({@code /{slug}}·{@code /{slug}/suggest}·{@code /{slug}/new}·{@code /{slug}/log}) 전용
 * 서버 인메모리 읽기 캐시. TTL 만료로만 갱신하며 무효화 훅은 두지 않는다
 * (dual SQL 적재는 앱 밖에서 일어나므로 TTL 이 지나면 자동 반영).
 *
 * <p>로그인·관리 화면은 이 캐시를 타지 않는다 — {@code PriceAlertViewController} 의 공개 핸들러에서만 사용.
 * 알람 스냅샷 캐시({@code AlertService#getCachedAlerts()}) 와 같은 "값 + TTL 나노초" 방식이다.
 */
@Component
public class PublicReadCache {

    private static final Logger log = LoggerFactory.getLogger(PublicReadCache.class);

    /** 캐시 항목 상한. 초과 시 만료분 정리 후에도 넘치면 전체 비움(공개 읽기 전용이라 재조회 비용만 발생). */
    private static final int MAX_ENTRIES = 300;

    private final ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();
    private final AppProperties appProperties;

    public PublicReadCache(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    private record Entry(Object value, long expiresAtNanos) {
        boolean isValid(long nowNanos) {
            return nowNanos - expiresAtNanos < 0;
        }
    }

    /**
     * {@code key} 에 살아 있는 값이 있으면 그대로, 없으면 {@code loader} 를 1회 실행해 저장 후 반환.
     * {@code loader} 예외는 캐시하지 않고 그대로 전파하며, {@code null} 결과도 저장하지 않는다.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, long ttlSeconds, Supplier<T> loader) {
        if (!appProperties.isPublicReadCacheEnabled() || ttlSeconds <= 0 || key == null) {
            return loader.get();
        }
        long now = System.nanoTime();
        Entry hit = entries.get(key);
        if (hit != null && hit.isValid(now)) {
            log.debug("[공개캐시] HIT {}", key);
            return (T) hit.value();
        }
        log.debug("[공개캐시] MISS {}", key);
        T fresh = loader.get();
        if (fresh == null) {
            entries.remove(key);
            return null;
        }
        put(key, freeze(fresh), now + ttlSeconds * 1_000_000_000L);
        return fresh;
    }

    private void put(String key, Object value, long expiresAtNanos) {
        if (entries.size() >= MAX_ENTRIES) {
            long now = System.nanoTime();
            entries.values().removeIf(e -> !e.isValid(now));
            if (entries.size() >= MAX_ENTRIES) {
                log.debug("[공개캐시] 상한({}) 초과 → 전체 비움", MAX_ENTRIES);
                entries.clear();
            }
        }
        entries.put(key, new Entry(value, expiresAtNanos));
    }

    /** 저장 값이 뷰·호출자 쪽에서 변형되지 않도록 리스트류는 불변 복사본으로 굳힌다. */
    private static Object freeze(Object value) {
        if (value instanceof List<?> list) {
            return List.copyOf(list);
        }
        if (value instanceof PriceAlertPageResult r) {
            return new PriceAlertPageResult(List.copyOf(r.list()), r.totalCount());
        }
        if (value instanceof PriceAlertTriggerPageResult r) {
            return new PriceAlertTriggerPageResult(List.copyOf(r.list()), r.totalCount());
        }
        if (value instanceof Map<?, ?> map) {
            return Map.copyOf(map);
        }
        return value;
    }
}

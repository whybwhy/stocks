package com.example.stocks.kis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * KIS Open API 호출 레이트 리미터 (앱키 단위 공유).
 * <p>실전 한도는 약 20 req/s, 모의는 약 2 req/s 입니다. 기본값은 안전 마진을 두고 15 req/s.</p>
 *
 * <p>설정: {@code kis.rest-rate-per-second} (기본 15) — 1 이상이어야 함.</p>
 *
 * <p>{@link #acquire()} 호출 시 직전 호출과의 최소 간격을 강제하여 폭주를 막습니다.
 * 단일 JVM·단일 앱키 가정. (멀티 인스턴스 환경이면 인스턴스 수만큼 분배해 설정.)</p>
 */
@Component
public class KisRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(KisRateLimiter.class);

    private final KisApiProperties properties;
    private long minIntervalNanos = 0L;
    private long lastAcquiredAtNanos = 0L;
    private final Object lock = new Object();

    public KisRateLimiter(KisApiProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        recalc();
        log.info("[KIS] rate limiter — {} req/s (min interval {} ms)",
                properties.getRestRatePerSecond(), TimeUnit.NANOSECONDS.toMillis(minIntervalNanos));
    }

    private void recalc() {
        int rate = Math.max(1, properties.getRestRatePerSecond());
        this.minIntervalNanos = TimeUnit.SECONDS.toNanos(1) / rate;
    }

    /**
     * 직전 acquire 와의 최소 간격을 보장합니다. 인터럽트 시 즉시 반환합니다.
     */
    public void acquire() {
        if (minIntervalNanos <= 0) return;
        long waitNs;
        synchronized (lock) {
            long now = System.nanoTime();
            long earliest = lastAcquiredAtNanos + minIntervalNanos;
            waitNs = earliest - now;
            if (waitNs <= 0) {
                lastAcquiredAtNanos = now;
                return;
            }
            lastAcquiredAtNanos = earliest;
        }
        try {
            TimeUnit.NANOSECONDS.sleep(waitNs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

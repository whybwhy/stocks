package com.example.stocks.fred;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * 경제 이벤트 스케줄러.
 *
 * 1) 매일 08:00 KST - D-1/당일 텔레그램 알림
 * 2) 매주 월요일 08:05 KST - FRED API에서 CPI/PPI/고용지표 발표일 동기화
 */
@Component
@ConditionalOnProperty(name = "economic-event.enabled", havingValue = "true", matchIfMissing = true)
public class EconomicEventScheduler {

    private static final Logger log = LoggerFactory.getLogger(EconomicEventScheduler.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final EconomicEventService eventService;

    public EconomicEventScheduler(EconomicEventService eventService) {
        this.eventService = eventService;
        log.info("[경제이벤트 스케줄러] 활성화됨");
    }

    /**
     * 매일 08:00 KST - D-1(내일)/D-0(오늘) 이벤트 텔레그램 알림.
     */
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    public void notifyDaily() {
        try {
            log.info("[경제이벤트] 일일 알림 체크 시작");
            eventService.notifyD1();
            eventService.notifyD0();
            log.info("[경제이벤트] 일일 알림 체크 완료");
        } catch (Exception e) {
            log.error("[경제이벤트] 일일 알림 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 매주 월요일 08:05 KST - FRED API 발표일 동기화.
     */
    @Scheduled(cron = "0 5 8 * * MON", zone = "Asia/Seoul")
    public void syncFredWeekly() {
        try {
            log.info("[경제이벤트] 주간 FRED 동기화 시작");
            eventService.syncFromFred();
            log.info("[경제이벤트] 주간 FRED 동기화 완료");
        } catch (Exception e) {
            log.error("[경제이벤트] FRED 동기화 실패: {}", e.getMessage(), e);
        }
    }
}

package com.example.stocks.kis;

import com.example.stocks.alert.TelegramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 2분마다 코스피·코스닥·야간선물 지수를 조회해 텔레그램으로 전송.
 *
 * 운영 시간:
 *  - 정규장: 평일 09:00 ~ 15:35 (코스피/코스닥)
 *  - 야간장: 평일 18:00 ~ 익일 05:00 (야간선물)
 *  위 시간 외에는 전송하지 않음.
 *
 * 비활성화: application.yml 에서 kis.market-index.enabled=false 설정
 */
@Component
@ConditionalOnProperty(name = "kis.market-index.enabled", havingValue = "true", matchIfMissing = true)
public class MarketIndexScheduler {

    private static final Logger log = LoggerFactory.getLogger(MarketIndexScheduler.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final KisIndexFetcher indexFetcher;
    private final TelegramService telegramService;

    public MarketIndexScheduler(KisIndexFetcher indexFetcher, TelegramService telegramService) {
        this.indexFetcher = indexFetcher;
        this.telegramService = telegramService;
        log.info("[지수스케줄러] 활성화 (2분 간격)");
    }

    @Scheduled(fixedDelayString = "${kis.market-index.interval-seconds:120}000", initialDelay = 30_000)
    public void run() {
        ZonedDateTime now = ZonedDateTime.now(KST);

        if (!isOperatingTime(now)) {
            log.debug("[지수스케줄러] 운영 시간 외 → 스킵 ({})", now.toLocalTime().format(TIME_FMT));
            return;
        }

        List<MarketIndexDto> indices = indexFetcher.fetchAll();
        if (indices.isEmpty()) {
            log.debug("[지수스케줄러] 조회 결과 없음 → 전송 스킵");
            return;
        }

        String msg = buildMessage(indices, now);
        telegramService.broadcast(msg);
        log.info("[지수스케줄러] 전송 완료: {}건", indices.size());
    }

    /**
     * 텔레그램 메시지 조립.
     * 예:
     *   📊 <b>시장 현황</b> (09:30)
     *   🔵 코스피    2,650.23  ▲ 15.45 (+0.59%)
     *   🟢 코스닥      850.12  ▼  8.23 (-0.96%)
     *   🌙 야간선물   350.25  ▲  2.15 (+0.62%)
     */
    private String buildMessage(List<MarketIndexDto> indices, ZonedDateTime now) {
        StringBuilder sb = new StringBuilder();
        sb.append("📊 <b>시장 현황</b> (").append(now.toLocalTime().format(TIME_FMT)).append(")\n");

        for (MarketIndexDto idx : indices) {
            String emoji = emojiFor(idx.getName());
            String arrow = idx.isUp() ? "▲" : idx.isDown() ? "▼" : "─";
            String sign  = idx.isUp() ? "+" : "";

            sb.append(String.format("%s %-6s %,10.2f  %s %,.2f (%s%,.2f%%)\n",
                    emoji,
                    idx.getName(),
                    idx.getCurrent(),
                    arrow,
                    idx.getChange() != null ? idx.getChange().abs() : BigDecimal.ZERO,
                    sign,
                    idx.getChangeRate() != null ? idx.getChangeRate() : BigDecimal.ZERO));
        }

        return sb.toString().trim();
    }

    private static String emojiFor(String name) {
        return switch (name) {
            case "코스피"   -> "🔵";
            case "코스닥"   -> "🟢";
            case "야간선물" -> "🌙";
            default         -> "📈";
        };
    }

    /**
     * 운영 시간 판단.
     *  - 정규장: 평일 09:00 ~ 15:35
     *  - 야간장: 평일 + 토요일 18:00 ~ 익일 05:00
     *    (금요일 야간장은 토요일 새벽까지 이어지므로 토요일 05:00까지 허용)
     */
    static boolean isOperatingTime(ZonedDateTime now) {
        DayOfWeek dow = now.getDayOfWeek();
        LocalTime t = now.toLocalTime();

        // 일요일은 운영 없음
        if (dow == DayOfWeek.SUNDAY) return false;

        // 토요일 새벽 00:00~05:00: 금요일 야간장 연장
        if (dow == DayOfWeek.SATURDAY) {
            return !t.isAfter(LocalTime.of(5, 0));
        }

        // 평일: 정규장 (09:00~15:35)
        boolean regular = !t.isBefore(LocalTime.of(9, 0)) && !t.isAfter(LocalTime.of(15, 35));
        // 평일: 야간장 (18:00~23:59)
        boolean night = !t.isBefore(LocalTime.of(18, 0));
        // 평일 자정~05:00: 전날 야간장 연장
        boolean earlyMorning = t.isBefore(LocalTime.of(5, 0));

        return regular || night || earlyMorning;
    }
}

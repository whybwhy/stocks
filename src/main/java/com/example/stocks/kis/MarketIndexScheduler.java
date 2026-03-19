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
 * 2분마다 시장 지수를 조회해 텔레그램으로 전송.
 *
 * 조회 대상: 코스피, 코스닥, 야간선물, 나스닥100, S&P500
 *
 * 운영 시간 (KST):
 *  - 정규장: 평일 09:00 ~ 15:35 (국내)
 *  - 야간장: 평일 18:00 ~ 익일 05:00 (야간선물 + 미국장)
 *  - 금→토: 토요일 00:00 ~ 05:00 (미국 금요 야간)
 *
 * 비활성화: kis.market-index.enabled=false
 */
@Component
@ConditionalOnProperty(name = "kis.market-index.enabled", havingValue = "true", matchIfMissing = true)
public class MarketIndexScheduler {

    private static final Logger log = LoggerFactory.getLogger(MarketIndexScheduler.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter TITLE_FMT = DateTimeFormatter.ofPattern("yyMMdd");
    private static final DateTimeFormatter TIME_FMT  = DateTimeFormatter.ofPattern("HH:mm");

    private final KisIndexFetcher indexFetcher;
    private final TelegramService telegramService;

    public MarketIndexScheduler(KisIndexFetcher indexFetcher, TelegramService telegramService) {
        this.indexFetcher = indexFetcher;
        this.telegramService = telegramService;
        log.info("[지수스케줄러] 활성화 (코스피/코스닥/야간선물/나스닥100/S&P500)");
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
     *
     * 📊 시장 현황 (10:30)
     * 🇰🇷 국내
     * 🔵 코스피    2,650.23  ▲ 15.45 (+0.59%)
     * 🟢 코스닥      850.12  ▼  8.23 (-0.96%)
     * 🌙 야간선물   350.25  ▲  2.15 (+0.62%)
     * 🇺🇸 미국
     * 🟠 나스닥100  18,245.67  ▲ 125.34 (+0.69%)
     * 🔴 S&P500    5,678.90  ▼  23.45 (-0.41%)
     */
    private String buildMessage(List<MarketIndexDto> indices, ZonedDateTime now) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(now.format(TITLE_FMT)).append("] 시장현황 (").append(now.toLocalTime().format(TIME_FMT)).append(")\n");

        boolean krHeaderAdded = false;
        boolean usHeaderAdded = false;

        for (MarketIndexDto idx : indices) {
            boolean isUS = isUsIndex(idx.getName());

            if (!isUS && !krHeaderAdded) {
                sb.append("\n🇰🇷 <b>국내</b>\n");
                krHeaderAdded = true;
            }
            if (isUS && !usHeaderAdded) {
                sb.append("\n🇺🇸 <b>미국</b>\n");
                usHeaderAdded = true;
            }

            String arrow = idx.isUp() ? "▲" : idx.isDown() ? "▼" : "─";
            String sign  = idx.isUp() ? "+" : "";

            sb.append(String.format("- %s  <b>%,.2f</b>  %s %,.2f (%s%,.2f%%)\n",
                    idx.getName(),
                    idx.getCurrent(),
                    arrow,
                    idx.getChange() != null ? idx.getChange().abs() : BigDecimal.ZERO,
                    sign,
                    idx.getChangeRate() != null ? idx.getChangeRate() : BigDecimal.ZERO));
        }

        return sb.toString().trim();
    }

    private static boolean isUsIndex(String name) {
        return "나스닥100".equals(name) || "S&P500".equals(name);
    }

    /**
     * 운영 시간 판단 (KST 기준).
     *  - 정규장: 평일 09:00 ~ 15:35
     *  - 야간장: 평일 18:00 ~ 23:59
     *  - 야간연장: 평일·토 00:00 ~ 05:00
     *  - 일요일: 운영 안 함
     */
    static boolean isOperatingTime(ZonedDateTime now) {
        DayOfWeek dow = now.getDayOfWeek();
        LocalTime t = now.toLocalTime();

        if (dow == DayOfWeek.SUNDAY) return false;

        if (dow == DayOfWeek.SATURDAY) {
            return !t.isAfter(LocalTime.of(5, 0));
        }

        boolean regular      = !t.isBefore(LocalTime.of(9, 0))  && !t.isAfter(LocalTime.of(15, 35));
        boolean night         = !t.isBefore(LocalTime.of(18, 0));
        boolean earlyMorning  = t.isBefore(LocalTime.of(5, 0));

        return regular || night || earlyMorning;
    }
}

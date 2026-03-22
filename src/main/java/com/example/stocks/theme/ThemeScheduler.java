package com.example.stocks.theme;

import com.example.stocks.alert.TelegramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 테마 시세를 하루 3회 텔레그램으로 발송.
 *
 * 08:30 - 전일 기준 테마 등락률 (장 시작 전 브리핑)
 * 10:00 - 장 초반 실시간 테마 등락률
 * 15:40 - 당일 최종 테마 등락률 (마감 정리)
 *
 * 비활성화: theme.enabled=false
 */
@Component
@ConditionalOnProperty(name = "theme.enabled", havingValue = "true", matchIfMissing = true)
public class ThemeScheduler {

    private static final Logger log = LoggerFactory.getLogger(ThemeScheduler.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd");

    private static final int TOP_N = 10;

    private final NaverThemeFetcher themeFetcher;
    private final TelegramService telegramService;

    public ThemeScheduler(NaverThemeFetcher themeFetcher, TelegramService telegramService) {
        this.themeFetcher = themeFetcher;
        this.telegramService = telegramService;
        log.info("[테마스케줄러] 활성화 (08:30 / 10:00 / 15:40)");
    }

    /** 08:30 - 장 시작 전 브리핑 (전일 기준) */
    @Scheduled(cron = "0 30 8 * * MON-FRI", zone = "Asia/Seoul")
    public void morningBriefing() {
        send("📋 장 시작 전 브리핑 (전일 기준)");
    }

    /** 10:00 - 장 초반 실시간 */
    @Scheduled(cron = "0 0 10 * * MON-FRI", zone = "Asia/Seoul")
    public void midMorning() {
        send("📊 오전장 테마 현황 (실시간)");
    }

    /** 15:40 - 마감 정리 */
    @Scheduled(cron = "0 40 15 * * MON-FRI", zone = "Asia/Seoul")
    public void closingBriefing() {
        send("🔔 마감 테마 정리 (당일 최종)");
    }

    private void send(String title) {
        try {
            List<ThemeDto> themes = themeFetcher.fetchTopThemes(TOP_N);
            if (themes.isEmpty()) {
                log.warn("[테마스케줄러] 조회 결과 없음 → 전송 스킵");
                return;
            }
            String msg = buildMessage(title, themes);
            telegramService.broadcast(msg);
            log.info("[테마스케줄러] '{}' 전송 완료 ({}건)", title, themes.size());
        } catch (Exception e) {
            log.error("[테마스케줄러] 전송 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 텔레그램 메시지 형식:
     *
     * 📋 장 시작 전 브리핑 (전일 기준) | 02/27 08:30
     * ─────────────────────
     *  1. 2026 하반기 신규상장 +34.05% (3일 -0.70%)
     *     ▲4 ─0 ▼5 | 레이저쎌.. · 카카오뱅크..
     *  2. ...
     */
    private String buildMessage(String title, List<ThemeDto> themes) {
        ZonedDateTime now = ZonedDateTime.now(KST);
        StringBuilder sb = new StringBuilder();

        sb.append("<b>").append(title).append("</b>")
          .append("  <i>").append(now.format(DATE_FMT))
          .append(" ").append(now.toLocalTime().format(TIME_FMT)).append("</i>\n");
        sb.append("─────────────────────\n");

        int rank = 1;
        for (ThemeDto t : themes) {
            String rateStr = t.getChangeRate();
            String rateEmoji = t.isPositive() ? "🔴" : t.isNegative() ? "🔵" : "⬜";

            sb.append(String.format("%2d. %s <b>%s</b>", rank, rateEmoji, t.getName()));

            if (rateStr != null && !rateStr.isBlank()) {
                sb.append("  ").append(formatRate(rateStr));
            }
            if (t.getRecent3DaysRate() != null && !t.getRecent3DaysRate().isBlank()) {
                sb.append("  <i>(3일 ").append(t.getRecent3DaysRate()).append(")</i>");
            }
            sb.append("\n");

            // 상승/보합/하락 + 주도주
            sb.append("     ▲").append(t.getUpCount())
              .append(" ─").append(t.getSteadyCount())
              .append(" ▼").append(t.getDownCount());

            String leaders = buildLeaders(t.getLeader1(), t.getLeader2());
            if (!leaders.isBlank()) {
                sb.append("  ").append(leaders);
            }
            sb.append("\n");

            rank++;
        }

        return sb.toString().trim();
    }

    private String formatRate(String rate) {
        if (rate.startsWith("+")) return "<b>" + rate + "</b>";
        if (rate.startsWith("-")) return "<b>" + rate + "</b>";
        return rate;
    }

    private String buildLeaders(String l1, String l2) {
        if ((l1 == null || l1.isBlank()) && (l2 == null || l2.isBlank())) return "";
        if (l2 == null || l2.isBlank()) return l1;
        if (l1 == null || l1.isBlank()) return l2;
        return l1 + " · " + l2;
    }
}

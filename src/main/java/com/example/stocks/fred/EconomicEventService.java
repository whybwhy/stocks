package com.example.stocks.fred;

import com.example.stocks.alert.TelegramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * economic_events Supabase CRUD + FRED 갱신 + 텔레그램 알림 발송.
 */
@Service
public class EconomicEventService {

    private static final Logger log = LoggerFactory.getLogger(EconomicEventService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RestClient supabaseRestClient;
    private final TelegramService telegramService;
    private final FredReleaseFetcher fredReleaseFetcher;

    public EconomicEventService(@Qualifier("supabaseRestClient") RestClient supabaseRestClient,
                                TelegramService telegramService,
                                FredReleaseFetcher fredReleaseFetcher) {
        this.supabaseRestClient = supabaseRestClient;
        this.telegramService = telegramService;
        this.fredReleaseFetcher = fredReleaseFetcher;
    }

    /**
     * FRED API에서 CPI/PPI/고용지표 미래 발표일을 조회하여 DB에 UPSERT.
     */
    public void syncFromFred() {
        if (!fredReleaseFetcher.isConfigured()) {
            log.info("[경제이벤트] FRED API key 미설정 → 동기화 스킵");
            return;
        }

        List<FredReleaseFetcher.FredReleaseDate> dates = fredReleaseFetcher.fetchUpcomingDates();
        int upserted = 0;
        for (FredReleaseFetcher.FredReleaseDate rd : dates) {
            try {
                upsertEvent(rd);
                upserted++;
            } catch (Exception e) {
                log.warn("[경제이벤트] UPSERT 실패: {} {} - {}", rd.eventDate(), rd.eventName(), e.getMessage());
            }
        }
        log.info("[경제이벤트] FRED 동기화 완료: {}건 UPSERT", upserted);
    }

    private void upsertEvent(FredReleaseFetcher.FredReleaseDate rd) {
        String body = String.format(
                "{\"event_date\":\"%s\",\"event_name\":\"%s\",\"description\":\"%s\",\"source\":\"FRED\",\"fred_release_id\":%d}",
                rd.eventDate().format(DATE_FMT),
                rd.eventName(),
                rd.description(),
                rd.fredReleaseId()
        );

        supabaseRestClient.post()
                .uri("/rest/v1/economic_events")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Prefer", "resolution=merge-duplicates")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * D-1 (내일) 이벤트 알림 발송.
     */
    public void notifyD1() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<EconomicEventDto> events = findByDateAndNotNotified(tomorrow, "notified_d1");
        if (events.isEmpty()) return;

        for (EconomicEventDto event : events) {
            String msg = buildMessage("내일", event, tomorrow);
            telegramService.broadcast(msg);
            markNotified(event.getId(), "notified_d1");
            log.info("[경제이벤트] D-1 알림: {} {}", event.getEventName(), event.getEventDate());
        }
    }

    /**
     * D-0 (오늘) 이벤트 알림 발송.
     */
    public void notifyD0() {
        LocalDate today = LocalDate.now();
        List<EconomicEventDto> events = findByDateAndNotNotified(today, "notified_d0");
        if (events.isEmpty()) return;

        for (EconomicEventDto event : events) {
            String msg = buildMessage("오늘", event, today);
            telegramService.broadcast(msg);
            markNotified(event.getId(), "notified_d0");
            log.info("[경제이벤트] D-0 알림: {} {}", event.getEventName(), event.getEventDate());
        }
    }

    private String buildMessage(String prefix, EconomicEventDto event, LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        String dayName = dow.getDisplayName(TextStyle.SHORT, Locale.KOREAN);

        StringBuilder sb = new StringBuilder();
        sb.append("[").append(prefix).append("] <b>").append(event.getEventName()).append("</b>\n");
        sb.append("날짜 : ").append(date.format(DATE_FMT)).append(" (").append(dayName).append(")");
        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            sb.append("\n").append(event.getDescription());
        }
        return sb.toString();
    }

    private List<EconomicEventDto> findByDateAndNotNotified(LocalDate date, String notifiedColumn) {
        try {
            List<EconomicEventDto> list = supabaseRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rest/v1/economic_events")
                            .queryParam("select", "*")
                            .queryParam("event_date", "eq." + date.format(DATE_FMT))
                            .queryParam(notifiedColumn, "eq.false")
                            .queryParam("order", "id.asc")
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<EconomicEventDto>>() {});
            return list != null ? list : List.of();
        } catch (Exception e) {
            log.error("[경제이벤트] 조회 실패 (date={}, col={}): {}", date, notifiedColumn, e.getMessage());
            return List.of();
        }
    }

    private void markNotified(Long id, String column) {
        try {
            String body = "{\"" + column + "\": true}";
            supabaseRestClient.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rest/v1/economic_events")
                            .queryParam("id", "eq." + id)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("[경제이벤트] {} 갱신 실패 (id={}): {}", column, id, e.getMessage());
        }
    }
}

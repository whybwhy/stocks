package com.example.stocks.fred;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FRED API fred/release/dates 엔드포인트로 경제지표 발표일 조회.
 *
 * release_id:
 *  - 10 = CPI (Consumer Price Index)
 *  - 46 = PPI (Producer Price Index)
 *  - 50 = Employment Situation (고용지표)
 */
@Component
public class FredReleaseFetcher {

    private static final Logger log = LoggerFactory.getLogger(FredReleaseFetcher.class);

    static final Map<Integer, String> RELEASE_MAP = Map.of(
            10, "CPI",
            46, "PPI",
            50, "고용지표"
    );

    static final Map<Integer, String> DESCRIPTION_MAP = Map.of(
            10, "소비자물가지수 (CPI) 발표",
            46, "생산자물가지수 (PPI) 발표",
            50, "비농업 고용지표 (NFP) 발표"
    );

    private final FredApiProperties properties;
    private final RestClient fredRestClient;
    private final ObjectMapper objectMapper;

    public FredReleaseFetcher(FredApiProperties properties,
                              @Qualifier("fredRestClient") RestClient fredRestClient,
                              ObjectMapper objectMapper) {
        this.properties = properties;
        this.fredRestClient = fredRestClient;
        this.objectMapper = objectMapper;
    }

    public boolean isConfigured() {
        return properties.isConfigured();
    }

    /**
     * 모든 추적 대상 릴리즈의 미래 발표일을 조회.
     *
     * @return (event_date, event_name, description, fred_release_id) 목록
     */
    public List<FredReleaseDate> fetchUpcomingDates() {
        List<FredReleaseDate> result = new ArrayList<>();
        if (!isConfigured()) {
            log.warn("[FRED] API key 미설정 → 스킵");
            return result;
        }

        for (Map.Entry<Integer, String> entry : RELEASE_MAP.entrySet()) {
            int releaseId = entry.getKey();
            String eventName = entry.getValue();
            String description = DESCRIPTION_MAP.getOrDefault(releaseId, eventName);
            try {
                List<LocalDate> dates = fetchReleaseDates(releaseId);
                LocalDate today = LocalDate.now();
                for (LocalDate d : dates) {
                    if (!d.isBefore(today)) {
                        result.add(new FredReleaseDate(d, eventName, description, releaseId));
                    }
                }
                log.info("[FRED] {} (release_id={}) → 미래 {}건 조회", eventName, releaseId,
                        dates.stream().filter(d -> !d.isBefore(today)).count());
            } catch (Exception e) {
                log.warn("[FRED] {} (release_id={}) 조회 실패: {}", eventName, releaseId, e.getMessage());
            }
        }
        return result;
    }

    private List<LocalDate> fetchReleaseDates(int releaseId) {
        String json = fredRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fred/release/dates")
                        .queryParam("release_id", releaseId)
                        .queryParam("api_key", properties.getApiKey())
                        .queryParam("file_type", "json")
                        .queryParam("include_release_dates_with_no_data", "true")
                        .queryParam("sort_order", "desc")
                        .queryParam("limit", 12)
                        .build())
                .retrieve()
                .body(String.class);

        List<LocalDate> dates = new ArrayList<>();
        if (json == null || json.isBlank()) return dates;

        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode releaseDates = root.path("release_dates");
            if (releaseDates.isArray()) {
                for (JsonNode node : releaseDates) {
                    String dateStr = node.path("date").asText(null);
                    if (dateStr != null && !dateStr.isBlank()) {
                        dates.add(LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[FRED] release_id={} 파싱 실패: {}", releaseId, e.getMessage());
        }
        return dates;
    }

    public record FredReleaseDate(LocalDate eventDate, String eventName, String description, int fredReleaseId) {}
}

package com.example.stocks.kis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * KIS 국내주식 일봉(기간별시세) 조회 — 전전일·전일 완성봉 + 당일(시가·현재가) 봉형태 문구 생성.
 */
@Component
public class KisDomesticDailyBarFetcher {

    private static final Logger log = LoggerFactory.getLogger(KisDomesticDailyBarFetcher.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.BASIC_ISO_DATE;
    private static final Pattern SIX_DIGIT = Pattern.compile(KisApiConstants.STOCK_CODE_PATTERN);

    private final KisApiProperties properties;
    private final KisTokenService tokenService;
    private final RestClient kisRestClient;
    private final ObjectMapper objectMapper;

    public KisDomesticDailyBarFetcher(KisApiProperties properties, KisTokenService tokenService,
                                      @Qualifier("kisRestClient") RestClient kisRestClient,
                                      ObjectMapper objectMapper) {
        this.properties = properties;
        this.tokenService = tokenService;
        this.kisRestClient = kisRestClient;
        this.objectMapper = objectMapper;
    }

    public boolean isConfigured() {
        return properties.isConfigured() && tokenService.getAccessToken() != null;
    }

    /**
     * "양봉 (전전일) -> 도지 (전일) -> 음봉 (오늘) => 빵 빵 뿡" 형식 (양봉·도지=빵, 음봉=뿡). 실패 시 빈 문자열.
     *
     * @param todayOpen   당일 시가 (inquire-price stck_oprc), 없으면 일봉 첫 행 시가 시도
     * @param currentPrice 당일 종가 대용(조회 시점 현재가)
     */
    public String buildThreeDayCandleLine(String stockCode, BigDecimal todayOpen, BigDecimal currentPrice) {
        if (stockCode == null || !SIX_DIGIT.matcher(stockCode.trim()).matches()
                || currentPrice == null) {
            return "";
        }
        if (!isConfigured()) return "";

        List<DailyBar> newestFirst = fetchDailyBarsNewestFirst(stockCode.trim());
        if (newestFirst.isEmpty()) return "";

        LocalDate today = LocalDate.now(KST);

        String todayWord = candleWord(todayOpen, currentPrice);
        DailyBar d1;
        DailyBar d2;

        DailyBar row0 = newestFirst.get(0);
        if (row0.date.equals(today)) {
            if (todayOpen == null && row0.open != null) {
                todayWord = candleWord(row0.open, currentPrice);
            }
            if (newestFirst.size() < 3) return "";
            d1 = newestFirst.get(1);
            d2 = newestFirst.get(2);
        } else {
            if (newestFirst.size() < 2) return "";
            d1 = newestFirst.get(0);
            d2 = newestFirst.get(1);
        }

        String w2 = candleWord(d2.open, d2.close);
        String w1 = candleWord(d1.open, d1.close);
        if (w2.isEmpty() || w1.isEmpty() || todayWord.isEmpty()) return "";

        String sounds = threeBreadSounds(w2, w1, todayWord);
        if (sounds.isEmpty()) return "";

        return w2 + " (전진일) -> " + w1 + " (전일) -> " + todayWord + " (오늘) => " + sounds;
    }

    /** 양봉·도지 → 빵, 음봉 → 뿡. 전전일·전일·오늘 순, 공백 구분. */
    private static String threeBreadSounds(String w2, String w1, String todayWord) {
        String s2 = candleToBreadSound(w2);
        String s1 = candleToBreadSound(w1);
        String s0 = candleToBreadSound(todayWord);
        if (s2.isEmpty() || s1.isEmpty() || s0.isEmpty()) return "";
        return s2 + " " + s1 + " " + s0;
    }

    private static String candleToBreadSound(String candleWord) {
        return switch (candleWord) {
            case "양봉" -> "빵";
            case "음봉" -> "뿡";
            case "도지" -> "빵";
            default -> "";
        };
    }

    static String candleWord(BigDecimal open, BigDecimal close) {
        if (open == null || close == null) return "";
        int c = open.compareTo(close);
        if (c < 0) return "양봉";
        if (c > 0) return "음봉";
        return "도지";
    }

    private List<DailyBar> fetchDailyBarsNewestFirst(String code) {
        String token = tokenService.getAccessToken();
        if (token == null) return List.of();

        try {
            String url = KisApiConstants.DAILY_ITEM_CHART_URL
                    + "?FID_COND_MRKT_DIV_CODE=" + KisApiConstants.FID_COND_MRKT_DIV_CODE
                    + "&FID_INPUT_ISCD=" + code
                    + "&FID_INPUT_DATE_1="
                    + "&FID_INPUT_DATE_2="
                    + "&FID_PERIOD_DIV_CODE=D";
            String json = kisRestClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + token)
                    .header("appkey", properties.getAppKey())
                    .header("appsecret", properties.getAppSecret())
                    .header("tr_id", KisApiConstants.TR_ID_DAILY_ITEM_CHART)
                    .header("custtype", "P")
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(json);
            if (!"0".equals(root.path("rt_cd").asText(""))) {
                log.debug("KIS daily chart error for {}: {}", code, root.path("msg1").asText(""));
                return List.of();
            }
            JsonNode out2 = root.path("output2");
            if (!out2.isArray() || out2.isEmpty()) return List.of();

            List<DailyBar> list = new ArrayList<>();
            for (JsonNode row : out2) {
                LocalDate d = parseDate(row);
                BigDecimal o = parsePrice(row, "stck_oprc", "oprc");
                BigDecimal c = parsePrice(row, "stck_clpr", "clpr");
                if (d != null && o != null && c != null) {
                    list.add(new DailyBar(d, o, c));
                }
            }
            list.sort(Comparator.comparing((DailyBar b) -> b.date).reversed());
            return list;
        } catch (Exception e) {
            log.warn("KIS daily chart fetch failed for {}: {}", code, e.getMessage());
            return List.of();
        }
    }

    private static LocalDate parseDate(JsonNode row) {
        String raw = firstText(row, "stck_bsop_date", "stck_bsop_dt", "date");
        if (raw == null || raw.isBlank()) return null;
        raw = raw.replace("-", "").trim();
        if (raw.length() >= 8) {
            try {
                return LocalDate.parse(raw.substring(0, 8), YYYYMMDD);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private static BigDecimal parsePrice(JsonNode row, String... fieldNames) {
        String raw = firstText(row, fieldNames);
        return parseDecimal(raw);
    }

    private static String firstText(JsonNode row, String... names) {
        for (String n : names) {
            String t = row.path(n).asText(null);
            if (t != null && !t.isBlank()) return t;
        }
        return null;
    }

    private static BigDecimal parseDecimal(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            BigDecimal v = new BigDecimal(raw.replace(",", "").trim());
            if (v.compareTo(BigDecimal.ZERO) <= 0) return null;
            return v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private record DailyBar(LocalDate date, BigDecimal open, BigDecimal close) {}
}

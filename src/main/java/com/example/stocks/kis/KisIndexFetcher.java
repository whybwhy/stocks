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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 한국투자증권 API로 국내·해외 시장 지수 조회.
 *
 * 국내: /uapi/domestic-stock/v1/quotations/inquire-index-price (코스피, 코스닥, 야간선물)
 * 해외: /uapi/overseas-price/v1/quotations/inquire-daily-chartprice (나스닥100, S&P500)
 */
@Component
public class KisIndexFetcher {

    private static final Logger log = LoggerFactory.getLogger(KisIndexFetcher.class);
    private static final String DOMESTIC_INDEX_URL = "/uapi/domestic-stock/v1/quotations/inquire-index-price";

    private final KisApiProperties properties;
    private final KisTokenService tokenService;
    private final RestClient kisRestClient;
    private final ObjectMapper objectMapper;

    public KisIndexFetcher(KisApiProperties properties,
                           KisTokenService tokenService,
                           @Qualifier("kisRestClient") RestClient kisRestClient,
                           ObjectMapper objectMapper) {
        this.properties = properties;
        this.tokenService = tokenService;
        this.kisRestClient = kisRestClient;
        this.objectMapper = objectMapper;
    }

    public boolean isConfigured() {
        return properties.isConfigured();
    }

    /**
     * 국내 + 해외 지수를 모두 조회.
     */
    public List<MarketIndexDto> fetchAll() {
        List<MarketIndexDto> result = new ArrayList<>();
        if (!properties.isConfigured()) {
            log.warn("[지수조회] KIS 미설정 → 스킵");
            return result;
        }

        String token = tokenService.getAccessToken();
        if (token == null) {
            log.warn("[지수조회] 토큰 없음 → 스킵");
            return result;
        }

        // 국내
        addDomesticIfPresent(result, token, KisApiConstants.INDEX_CODE_KOSPI,         "코스피");
        addDomesticIfPresent(result, token, KisApiConstants.INDEX_CODE_KOSDAQ,        "코스닥");
        addDomesticIfPresent(result, token, KisApiConstants.INDEX_CODE_NIGHT_FUTURES, "야간선물");

        // 해외
        addOverseasIfPresent(result, token, KisApiConstants.OVERSEAS_NDX, "나스닥100");
        addOverseasIfPresent(result, token, KisApiConstants.OVERSEAS_SPX, "S&P500");

        return result;
    }

    // ─── 국내 업종지수 ───

    private void addDomesticIfPresent(List<MarketIndexDto> result, String token,
                                      String indexCode, String name) {
        try {
            MarketIndexDto dto = fetchDomesticIndex(token, indexCode, name);
            if (dto != null) result.add(dto);
        } catch (Exception e) {
            log.warn("[지수조회] {} 조회 실패: {}", name, e.getMessage());
        }
    }

    private MarketIndexDto fetchDomesticIndex(String token, String indexCode, String name) throws Exception {
        String url = DOMESTIC_INDEX_URL
                + "?FID_COND_MRKT_DIV_CODE=" + KisApiConstants.FID_COND_MRKT_DIV_CODE_INDEX
                + "&FID_INPUT_ISCD=" + indexCode;

        String json = kisRestClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .header("appkey",    properties.getAppKey())
                .header("appsecret", properties.getAppSecret())
                .header("tr_id",     KisApiConstants.TR_ID_INDEX)
                .header("custtype",  "P")
                .retrieve()
                .body(String.class);

        if (json == null || json.isBlank()) return null;

        JsonNode root = objectMapper.readTree(json);
        if (!"0".equals(root.path("rt_cd").asText(""))) {
            log.debug("[지수조회] {} 오류: rt_cd={} msg={}", name, root.path("rt_cd").asText(), root.path("msg1").asText());
            return null;
        }

        JsonNode output = root.path("output");
        if (output.isMissingNode()) return null;

        BigDecimal current    = parseBigDecimal(output.path("bstp_nmix_prpr").asText("0"));
        BigDecimal change     = parseBigDecimal(output.path("bstp_nmix_prdy_vrss").asText("0"));
        BigDecimal changeRate = parseBigDecimal(output.path("bstp_nmix_prdy_ctrt").asText("0"));
        String sign           = output.path("prdy_vrss_sign").asText("3");

        if (current == null || current.compareTo(BigDecimal.ZERO) == 0) {
            log.debug("[지수조회] {} 지수값 0 (장 미운영 가능성)", name);
            return null;
        }

        return new MarketIndexDto(name, current, change, changeRate, sign);
    }

    // ─── 해외 지수 (inquire-daily-chartprice) ───

    private void addOverseasIfPresent(List<MarketIndexDto> result, String token,
                                      String symbol, String name) {
        try {
            MarketIndexDto dto = fetchOverseasIndex(token, symbol, name);
            if (dto != null) result.add(dto);
        } catch (Exception e) {
            log.warn("[해외지수] {} 조회 실패: {}", name, e.getMessage());
        }
    }

    /**
     * inquire-daily-chartprice API로 해외 지수 일별 시세 조회.
     * 최근 7일 범위로 요청 후 가장 최신 레코드의 종가를 현재 지수로 사용.
     */
    private MarketIndexDto fetchOverseasIndex(String token, String symbol, String name) throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        String dateFrom = weekAgo.format(DateTimeFormatter.BASIC_ISO_DATE);
        String dateTo   = today.format(DateTimeFormatter.BASIC_ISO_DATE);

        String url = KisApiConstants.OVERSEAS_CHART_URL
                + "?FID_COND_MRKT_DIV_CODE=" + KisApiConstants.OVERSEAS_MRKT_CODE
                + "&FID_INPUT_ISCD=" + symbol
                + "&FID_INPUT_DATE_1=" + dateFrom
                + "&FID_INPUT_DATE_2=" + dateTo
                + "&FID_PERIOD_DIV_CODE=D";

        String json = kisRestClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .header("appkey",    properties.getAppKey())
                .header("appsecret", properties.getAppSecret())
                .header("tr_id",     KisApiConstants.TR_ID_OVERSEAS_CHART)
                .header("custtype",  "P")
                .retrieve()
                .body(String.class);

        if (json == null || json.isBlank()) {
            log.debug("[해외지수] {} 응답 없음", name);
            return null;
        }

        JsonNode root = objectMapper.readTree(json);
        String rtCd = root.path("rt_cd").asText("");

        if (!"0".equals(rtCd)) {
            log.warn("[해외지수] {} 오류: rt_cd={} msg_cd={} msg={}",
                    name, rtCd, root.path("msg_cd").asText(), root.path("msg1").asText());
            return null;
        }

        // output1: 요약 정보, output2: 일별 데이터 배열
        JsonNode output1 = root.path("output1");
        JsonNode output2 = root.path("output2");

        // output1 에서 현재가를 먼저 시도
        MarketIndexDto fromOutput1 = parseOverseasOutput1(output1, name);
        if (fromOutput1 != null) return fromOutput1;

        // output2 에서 가장 최근 레코드 사용
        if (output2.isArray() && !output2.isEmpty()) {
            JsonNode latest = output2.get(0);
            log.info("[해외지수] {} output2 첫 레코드 필드: {}", name, latest.toString().substring(0, Math.min(300, latest.toString().length())));
            return parseOverseasRecord(latest, name);
        }

        log.warn("[해외지수] {} output1/output2 모두 비어 있음. raw={}", name, json.substring(0, Math.min(500, json.length())));
        return null;
    }

    /**
     * output1 에서 현재가 파싱 시도.
     * 필드명이 API 버전에 따라 다를 수 있어 여러 후보를 시도.
     */
    private MarketIndexDto parseOverseasOutput1(JsonNode output1, String name) {
        if (output1 == null || output1.isMissingNode() || output1.isEmpty()) return null;

        log.info("[해외지수] {} output1 필드: {}", name, output1.toString().substring(0, Math.min(300, output1.toString().length())));

        BigDecimal current = firstNonNull(output1,
                "ovrs_nmix_prpr", "stck_prpr", "ovrs_prod_prpr", "last", "clos", "close");
        if (current == null || current.compareTo(BigDecimal.ZERO) == 0) return null;

        BigDecimal change = firstNonNull(output1,
                "ovrs_nmix_prdy_vrss", "prdy_vrss", "ovrs_prod_prdy_vrss", "diff");
        BigDecimal changeRate = firstNonNull(output1,
                "ovrs_nmix_prdy_ctrt", "prdy_ctrt", "ovrs_prod_prdy_ctrt", "rate");
        String sign = firstNonNullStr(output1, "prdy_vrss_sign", "sign");

        if (sign == null || sign.isBlank()) {
            sign = (change != null && change.compareTo(BigDecimal.ZERO) > 0) ? "2"
                 : (change != null && change.compareTo(BigDecimal.ZERO) < 0) ? "5" : "3";
        }

        return new MarketIndexDto(name, current, change, changeRate, sign);
    }

    /**
     * output2 일별 레코드에서 종가/등락 파싱.
     */
    private MarketIndexDto parseOverseasRecord(JsonNode record, String name) {
        BigDecimal current = firstNonNull(record,
                "ovrs_nmix_prpr", "clos", "close", "stck_clpr", "stck_prpr", "ovrs_prod_prpr", "last");
        if (current == null || current.compareTo(BigDecimal.ZERO) == 0) {
            log.debug("[해외지수] {} 레코드에서 종가 파싱 실패", name);
            return null;
        }

        BigDecimal change = firstNonNull(record,
                "ovrs_nmix_prdy_vrss", "prdy_vrss", "diff", "ovrs_prod_prdy_vrss");
        BigDecimal changeRate = firstNonNull(record,
                "ovrs_nmix_prdy_ctrt", "prdy_ctrt", "rate", "ovrs_prod_prdy_ctrt");
        String sign = firstNonNullStr(record, "prdy_vrss_sign", "sign", "fltt_rt_sign");

        if (sign == null || sign.isBlank()) {
            sign = (change != null && change.compareTo(BigDecimal.ZERO) > 0) ? "2"
                 : (change != null && change.compareTo(BigDecimal.ZERO) < 0) ? "5" : "3";
        }

        return new MarketIndexDto(name, current, change, changeRate, sign);
    }

    private BigDecimal firstNonNull(JsonNode node, String... fields) {
        for (String field : fields) {
            String val = node.path(field).asText(null);
            BigDecimal bd = parseBigDecimal(val);
            if (bd != null && bd.compareTo(BigDecimal.ZERO) != 0) return bd;
        }
        return null;
    }

    private String firstNonNullStr(JsonNode node, String... fields) {
        for (String field : fields) {
            String val = node.path(field).asText(null);
            if (val != null && !val.isBlank()) return val;
        }
        return null;
    }

    private static BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new BigDecimal(value.replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

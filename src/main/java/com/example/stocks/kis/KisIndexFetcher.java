package com.example.stocks.kis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 한국투자증권 업종지수 API로 코스피·코스닥·야간선물 지수 조회.
 *
 * 엔드포인트: /uapi/domestic-stock/v1/quotations/inquire-index-price
 * TR_ID      : FHPUP02100000
 * 시장구분    : U (업종)
 * 업종코드    : 0001(코스피), 1001(코스닥), 3003(야간선물)
 */
@Component
public class KisIndexFetcher {

    private static final Logger log = LoggerFactory.getLogger(KisIndexFetcher.class);
    private static final String INDEX_URL = "/uapi/domestic-stock/v1/quotations/inquire-index-price";

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
     * 코스피·코스닥·야간선물 지수를 한 번에 조회.
     * 개별 조회 실패 시 해당 항목은 결과에서 제외.
     *
     * @return 조회 성공한 지수 목록 (순서: 코스피 → 코스닥 → 야간선물)
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

        addIfPresent(result, token, KisApiConstants.INDEX_CODE_KOSPI,         "코스피");
        addIfPresent(result, token, KisApiConstants.INDEX_CODE_KOSDAQ,        "코스닥");
        addIfPresent(result, token, KisApiConstants.INDEX_CODE_NIGHT_FUTURES, "야간선물");

        return result;
    }

    private void addIfPresent(List<MarketIndexDto> result, String token, String indexCode, String name) {
        try {
            MarketIndexDto dto = fetchIndex(token, indexCode, name);
            if (dto != null) {
                result.add(dto);
            }
        } catch (Exception e) {
            log.warn("[지수조회] {} 조회 실패: {}", name, e.getMessage());
        }
    }

    private MarketIndexDto fetchIndex(String token, String indexCode, String name) throws Exception {
        String url = INDEX_URL
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

        if (json == null || json.isBlank()) {
            log.debug("[지수조회] {} 응답 없음", name);
            return null;
        }

        JsonNode root = objectMapper.readTree(json);
        String rtCd = root.path("rt_cd").asText("");
        if (!"0".equals(rtCd)) {
            log.debug("[지수조회] {} 오류: rt_cd={} msg={}", name, rtCd, root.path("msg1").asText());
            return null;
        }

        JsonNode output = root.path("output");
        if (output.isMissingNode()) {
            log.debug("[지수조회] {} output 없음", name);
            return null;
        }

        String currentStr    = output.path("bstp_nmix_prpr").asText("0");
        String changeStr     = output.path("bstp_nmix_prdy_vrss").asText("0");
        String changeRateStr = output.path("bstp_nmix_prdy_ctrt").asText("0");
        String sign          = output.path("prdy_vrss_sign").asText("3");

        BigDecimal current    = parseBigDecimal(currentStr);
        BigDecimal change     = parseBigDecimal(changeStr);
        BigDecimal changeRate = parseBigDecimal(changeRateStr);

        if (current == null || current.compareTo(BigDecimal.ZERO) == 0) {
            log.debug("[지수조회] {} 지수값 0 또는 null (장 미운영 가능성)", name);
            return null;
        }

        log.debug("[지수조회] {} = {} ({}{} / {}%)", name, current,
                sign.equals("2") ? "+" : sign.equals("5") ? "-" : "",
                change, changeRate);

        return new MarketIndexDto(name, current, change, changeRate, sign);
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

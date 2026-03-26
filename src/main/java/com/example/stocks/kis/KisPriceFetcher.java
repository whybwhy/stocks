package com.example.stocks.kis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 한국투자증권 API로 국내주식 현재가 조회.
 * 종목코드 6자리(FID_INPUT_ISCD)로 주식현재가 API 호출.
 */
@Component
public class KisPriceFetcher {

    private static final Logger log = LoggerFactory.getLogger(KisPriceFetcher.class);
    private static final Pattern SIX_DIGIT = Pattern.compile(KisApiConstants.STOCK_CODE_PATTERN);

    private final KisApiProperties properties;
    private final KisTokenService tokenService;
    private final RestClient kisRestClient;
    private final ObjectMapper objectMapper;

    public KisPriceFetcher(KisApiProperties properties, KisTokenService tokenService,
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
     * 국내 주식 현재가 조회. 6자리 종목코드만 사용.
     *
     * @param stockCodes 6자리 종목코드 집합 (예: 005930, 079550)
     * @return stock_code -> 현재가 (실패 시 해당 종목 제외)
     */
    public Map<String, BigDecimal> fetchPrices(Set<String> stockCodes) {
        Map<String, DomesticStockQuote> quotes = fetchQuotes(stockCodes);
        Map<String, BigDecimal> result = new HashMap<>();
        for (Map.Entry<String, DomesticStockQuote> e : quotes.entrySet()) {
            result.put(e.getKey(), e.getValue().currentPrice());
        }
        return result;
    }

    /**
     * 국내 주식 현재가·시가·당일고가·전일고가 조회 (inquire-price output).
     */
    public Map<String, DomesticStockQuote> fetchQuotes(Set<String> stockCodes) {
        Map<String, DomesticStockQuote> result = new HashMap<>();
        if (stockCodes == null || stockCodes.isEmpty()) return result;
        String token = tokenService.getAccessToken();
        if (token == null) return result;

        for (String code : stockCodes) {
            if (code == null || !isValidStockCode(code)) continue;
            String normalized = code.trim();
            try {
                DomesticStockQuote q = fetchSingleQuote(token, normalized);
                if (q != null && q.currentPrice() != null && q.currentPrice().compareTo(BigDecimal.ZERO) > 0) {
                    result.put(normalized, q);
                }
            } catch (Exception e) {
                log.warn("KIS price fetch failed for {}: {}", normalized, e.getMessage());
            }
        }
        return result;
    }

    private static boolean isValidStockCode(String code) {
        return code != null && code.trim().length() == 6 && SIX_DIGIT.matcher(code.trim()).matches();
    }

    private DomesticStockQuote fetchSingleQuote(String token, String fidInputIscd) {
        try {
            String url = "/uapi/domestic-stock/v1/quotations/inquire-price"
                    + "?FID_COND_MRKT_DIV_CODE=" + KisApiConstants.FID_COND_MRKT_DIV_CODE
                    + "&FID_INPUT_ISCD=" + fidInputIscd;
            String json = kisRestClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + token)
                    .header("appkey", properties.getAppKey())
                    .header("appsecret", properties.getAppSecret())
                    .header("tr_id", KisApiConstants.TR_ID_PRICE)
                    .header("custtype", "P")
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(json);
            String rtCd = root.path("rt_cd").asText("");
            if (!"0".equals(rtCd)) {
                String msgCd = root.path("msg_cd").asText("");
                String msg = root.path("msg1").asText("");
                log.debug("KIS inquire-price error for {}: rt_cd={} msg_cd={} msg={}", fidInputIscd, rtCd, msgCd, msg);
                return null;
            }

            JsonNode output = root.path("output");
            if (output.isMissingNode()) {
                log.debug("KIS inquire-price no output for {}", fidInputIscd);
                return null;
            }
            String stckPrpr = output.path("stck_prpr").asText(null);
            if (stckPrpr == null || stckPrpr.isBlank()) return null;
            BigDecimal current = new BigDecimal(stckPrpr.replace(",", ""));
            BigDecimal open = parseOptionalPrice(output.path("stck_oprc").asText(null));
            BigDecimal dayHigh = parseOptionalPrice(output.path("stck_hgpr").asText(null));
            BigDecimal prevDayHigh = firstPresentPrice(output, "prdy_hgpr", "stck_prdy_hgpr");
            return new DomesticStockQuote(current, open, dayHigh, prevDayHigh);
        } catch (Exception e) {
            log.warn("KIS fetchSingleQuote failed for {}: {}", fidInputIscd, e.getMessage());
            return null;
        }
    }

    private static BigDecimal parseOptionalPrice(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            BigDecimal v = new BigDecimal(raw.replace(",", ""));
            if (v.compareTo(BigDecimal.ZERO) <= 0) return null;
            return v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static BigDecimal firstPresentPrice(JsonNode output, String... fieldNames) {
        for (String name : fieldNames) {
            BigDecimal v = parseOptionalPrice(output.path(name).asText(null));
            if (v != null) return v;
        }
        return null;
    }
}

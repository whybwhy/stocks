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

/**
 * 한국투자증권 해외주식 현재가상세 API (price-detail) 로 미국 주식 현재가 조회.
 *
 * 엔드포인트: /uapi/overseas-price/v1/quotations/price-detail
 * TR_ID     : HHDFS76200200 (실전)
 * 가격 단위  : USD (달러)
 *
 * EXCD 거래소 코드: NAS(나스닥), NYS(뉴욕), AMS(아멕스)
 */
@Component
public class KisOverseasPriceFetcher {

    private static final Logger log = LoggerFactory.getLogger(KisOverseasPriceFetcher.class);

    private final KisApiProperties properties;
    private final KisTokenService tokenService;
    private final RestClient kisRestClient;
    private final ObjectMapper objectMapper;

    public KisOverseasPriceFetcher(KisApiProperties properties,
                                   KisTokenService tokenService,
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
     * 해외주식 현재가 조회.
     *
     * @param excd    거래소 코드 (NAS, NYS, AMS)
     * @param symbols 티커 심볼 집합 (AAPL, TSLA 등)
     * @return symbol → 현재가(USD)
     */
    public Map<String, BigDecimal> fetchPrices(String excd, Set<String> symbols) {
        Map<String, BigDecimal> result = new HashMap<>();
        if (symbols == null || symbols.isEmpty()) return result;
        String token = tokenService.getAccessToken();
        if (token == null) return result;

        for (String symbol : symbols) {
            if (symbol == null || symbol.isBlank()) continue;
            String trimmed = symbol.trim().toUpperCase();
            try {
                BigDecimal price = fetchSinglePrice(token, excd, trimmed);
                if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                    result.put(trimmed, price);
                }
            } catch (Exception e) {
                log.warn("[해외현재가] {} ({}) 조회 실패: {}", trimmed, excd, e.getMessage());
            }
        }
        return result;
    }

    private BigDecimal fetchSinglePrice(String token, String excd, String symbol) {
        try {
            String url = KisApiConstants.OVERSEAS_PRICE_URL
                    + "?AUTH="
                    + "&EXCD=" + excd
                    + "&SYMB=" + symbol;

            String json = kisRestClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + token)
                    .header("appkey",    properties.getAppKey())
                    .header("appsecret", properties.getAppSecret())
                    .header("tr_id",     KisApiConstants.TR_ID_OVERSEAS_PRICE)
                    .header("custtype",  "P")
                    .retrieve()
                    .body(String.class);

            if (json == null || json.isBlank()) return null;

            JsonNode root = objectMapper.readTree(json);
            String rtCd = root.path("rt_cd").asText("");
            if (!"0".equals(rtCd)) {
                log.debug("[해외현재가] {} 오류: rt_cd={} msg={}", symbol, rtCd, root.path("msg1").asText());
                return null;
            }

            JsonNode output = root.path("output");
            if (output.isMissingNode()) return null;

            // last: 현재가 (장중) / base: 전일종가
            String lastStr = output.path("last").asText(null);
            if (lastStr == null || lastStr.isBlank()) {
                lastStr = output.path("base").asText(null);
            }
            if (lastStr == null || lastStr.isBlank()) return null;

            log.debug("[해외현재가] {} ({}) = ${}", symbol, excd, lastStr);
            return new BigDecimal(lastStr.replace(",", ""));
        } catch (Exception e) {
            log.warn("[해외현재가] {} 파싱 실패: {}", symbol, e.getMessage());
            return null;
        }
    }
}

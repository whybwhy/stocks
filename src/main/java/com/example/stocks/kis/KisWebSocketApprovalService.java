package com.example.stocks.kis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;

/**
 * 한국투자증권 WebSocket 접속 승인키 발급 및 캐시.
 * REST /oauth2/Approval 으로 approval_key 발급 (유효기간 24시간).
 */
@Service
public class KisWebSocketApprovalService {

    private static final Logger log = LoggerFactory.getLogger(KisWebSocketApprovalService.class);
    private static final long EXPIRE_BUFFER_SECONDS = 300;

    private final KisApiProperties properties;
    private final RestClient kisRestClient;
    private final ObjectMapper objectMapper;

    private String cachedKey;
    private Instant expiresAt;

    public KisWebSocketApprovalService(KisApiProperties properties,
                                       @Qualifier("kisRestClient") RestClient kisRestClient,
                                       ObjectMapper objectMapper) {
        this.properties = properties;
        this.kisRestClient = kisRestClient;
        this.objectMapper = objectMapper;
    }

    /**
     * WebSocket 접속용 승인키 반환. 없거나 만료 임박 시 재발급.
     */
    public String getApprovalKey() {
        if (!properties.isConfigured()) return null;
        if (cachedKey != null && expiresAt != null
                && Instant.now().plusSeconds(EXPIRE_BUFFER_SECONDS).isBefore(expiresAt)) {
            return cachedKey;
        }
        return refreshKey();
    }

    private synchronized String refreshKey() {
        if (cachedKey != null && expiresAt != null
                && Instant.now().plusSeconds(EXPIRE_BUFFER_SECONDS).isBefore(expiresAt)) {
            return cachedKey;
        }
        try {
            log.info("[KIS WS] approval_key 발급 요청 → /oauth2/Approval");
            String body = """
                    {"grant_type":"client_credentials","appkey":"%s","secretkey":"%s"}
                    """.formatted(properties.getAppKey(), properties.getAppSecret());
            String json = kisRestClient.post()
                    .uri("/oauth2/Approval")
                    .body(body)
                    .retrieve()
                    .body(String.class);
            log.info("[KIS WS] approval_key 응답: {}", json != null ? json.substring(0, Math.min(200, json.length())) : "null");

            JsonNode root = objectMapper.readTree(json);
            cachedKey = root.path("approval_key").asText(null);
            expiresAt = Instant.now().plusSeconds(86400);
            log.info("[KIS WS] approval_key 발급 완료 (길이={})", cachedKey != null ? cachedKey.length() : 0);
            return cachedKey;
        } catch (Exception e) {
            log.error("[KIS WS] approval_key 발급 실패: {} ({})", e.getMessage(), e.getClass().getSimpleName(), e);
            return null;
        }
    }
}

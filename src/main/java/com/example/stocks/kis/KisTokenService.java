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
 * 한국투자증권 OAuth2 접근 토큰 발급 및 캐시.
 * 토큰 유효기간 24시간, 만료 전 갱신.
 */
@Service
public class KisTokenService {

    private static final Logger log = LoggerFactory.getLogger(KisTokenService.class);
    /** 만료 5분 전이면 재발급 */
    private static final long EXPIRE_BUFFER_SECONDS = 300;

    private final KisApiProperties properties;
    private final RestClient kisRestClient;
    private final ObjectMapper objectMapper;

    private String cachedToken;
    private Instant expiresAt;

    public KisTokenService(KisApiProperties properties, @Qualifier("kisRestClient") RestClient kisRestClient, ObjectMapper objectMapper) {
        this.properties = properties;
        this.kisRestClient = kisRestClient;
        this.objectMapper = objectMapper;
    }

    public boolean isConfigured() {
        return properties.isConfigured();
    }

    /**
     * 접근 토큰 반환. 없거나 만료 임박 시 재발급.
     */
    public String getAccessToken() {
        if (!properties.isConfigured()) return null;
        if (cachedToken != null && expiresAt != null && Instant.now().plusSeconds(EXPIRE_BUFFER_SECONDS).isBefore(expiresAt)) {
            return cachedToken;
        }
        return refreshToken();
    }

    private synchronized String refreshToken() {
        if (cachedToken != null && expiresAt != null && Instant.now().plusSeconds(EXPIRE_BUFFER_SECONDS).isBefore(expiresAt)) {
            return cachedToken;
        }
        try {
            String body = """
                    {"grant_type":"client_credentials","appkey":"%s","appsecret":"%s"}
                    """.formatted(properties.getAppKey(), properties.getAppSecret());
            String json = kisRestClient.post()
                    .uri("/oauth2/tokenP")
                    .body(body)
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(json);
            cachedToken = root.path("access_token").asText(null);
            int expiresIn = root.path("expires_in").asInt(86400);
            expiresAt = Instant.now().plusSeconds(expiresIn);
            log.info("KIS token refreshed, expires in {}s", expiresIn);
            return cachedToken;
        } catch (Exception e) {
            log.error("KIS token refresh failed: {}", e.getMessage());
            return null;
        }
    }
}

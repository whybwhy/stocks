package com.example.stocks.kis;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 한국투자증권 Open API 설정.
 * KIS Developers 앱 등록 후 APP_KEY, APP_SECRET 발급.
 */
@ConfigurationProperties(prefix = "kis")
public class KisApiProperties {

    /** 앱 키 (Client ID) */
    private String appKey = "";
    /** 앱 시크릿 (Client Secret) */
    private String appSecret = "";
    /** 실전: https://openapi.koreainvestment.com:9443, 모의: https://openapivts.koreainvestment.com:29443 */
    private String baseUrl = "https://openapi.koreainvestment.com:9443";
    /** websocket(기본) 또는 rest */
    private String mode = "websocket";

    /**
     * REST 호출 레이트 제한 (req/s). 실전 한도 ≈ 20, 모의 ≈ 2.
     * 안전 마진을 두고 기본 15. 환경변수: {@code KIS_REST_RATE_PER_SECOND}.
     */
    private int restRatePerSecond = 15;

    public String getAppKey() { return appKey; }
    public void setAppKey(String appKey) { this.appKey = appKey != null ? appKey : ""; }

    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret != null ? appSecret : ""; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl != null ? baseUrl : ""; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode != null ? mode : "websocket"; }

    public int getRestRatePerSecond() { return restRatePerSecond; }
    public void setRestRatePerSecond(int restRatePerSecond) {
        this.restRatePerSecond = restRatePerSecond > 0 ? restRatePerSecond : 15;
    }

    public boolean isConfigured() {
        return appKey != null && !appKey.isBlank() && appSecret != null && !appSecret.isBlank();
    }
}

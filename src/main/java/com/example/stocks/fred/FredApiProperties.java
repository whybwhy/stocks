package com.example.stocks.fred;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * FRED API 설정.
 * https://fred.stlouisfed.org/docs/api/api_key.html 에서 무료 발급.
 */
@ConfigurationProperties(prefix = "fred")
public class FredApiProperties {

    private String apiKey = "";

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey != null ? apiKey : ""; }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}

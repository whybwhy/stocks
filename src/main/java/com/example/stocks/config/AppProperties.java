package com.example.stocks.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * application.yml 의 app.* 설정.
 * allowed-kakao-accounts: /stock/chartboy 접근 허용 카카오 닉네임 목록. 비어 있으면 모든 인증 사용자 허용.
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private List<String> allowedKakaoAccounts = new ArrayList<>();

    public List<String> getAllowedKakaoAccounts() {
        return allowedKakaoAccounts;
    }

    public void setAllowedKakaoAccounts(List<String> allowedKakaoAccounts) {
        this.allowedKakaoAccounts = allowedKakaoAccounts != null ? allowedKakaoAccounts : new ArrayList<>();
    }
}

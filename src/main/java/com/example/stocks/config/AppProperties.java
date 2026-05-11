package com.example.stocks.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * application.yml 의 app.* 설정.
 * allowed-kakao-accounts: /stock/chartboy 접근 허용 카카오 닉네임 목록. 비어 있으면 모든 인증 사용자 허용.
 * allowed-user-emails: 비어 있지 않으면 카카오 로그인 사용자의 이메일이 목록에 있을 때만 앱 접근(공개 경로 제외).
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * 공개 price_alerts 목록·돌파 로그 URL 접두사 ({@code /{slug}/list}, {@code /{slug}/log}).
     * 슬래시 없이 영문·숫자만 (예: {@code chartboy}, {@code x7k9m2}).
     */
    private String priceAlertPublicSlug = "chartboy";

    private List<String> allowedKakaoAccounts = new ArrayList<>();

    /**
     * 콤마로 구분된 이메일 목록. 환경변수 예: {@code APP_ALLOWED_USER_EMAILS=whybwhy@gmail.com}.
     * 비어 있으면 이메일 화이트리스트 검사를 하지 않음.
     */
    private String allowedUserEmails = "";

    public List<String> getAllowedKakaoAccounts() {
        return allowedKakaoAccounts;
    }

    public void setAllowedKakaoAccounts(List<String> allowedKakaoAccounts) {
        this.allowedKakaoAccounts = allowedKakaoAccounts != null ? allowedKakaoAccounts : new ArrayList<>();
    }

    public String getPriceAlertPublicSlug() {
        return priceAlertPublicSlug;
    }

    public void setPriceAlertPublicSlug(String priceAlertPublicSlug) {
        this.priceAlertPublicSlug = priceAlertPublicSlug;
    }

    /** {@code app.price-alert-public-slug} 정규화 (빈 값·슬래시 제거, 미허용 문자는 chartboy). */
    public String normalizedPriceAlertPublicSlug() {
        if (priceAlertPublicSlug == null || priceAlertPublicSlug.isBlank()) {
            return "chartboy";
        }
        String slug = priceAlertPublicSlug.trim();
        if (slug.startsWith("/")) {
            slug = slug.substring(1);
        }
        if (slug.endsWith("/")) {
            slug = slug.substring(0, slug.length() - 1);
        }
        if (!slug.matches("[A-Za-z0-9]{1,32}")) {
            return "chartboy";
        }
        return slug;
    }

    public String publicPriceAlertListPath() {
        return "/" + normalizedPriceAlertPublicSlug() + "/list";
    }

    public String publicPriceAlertLogPath() {
        return "/" + normalizedPriceAlertPublicSlug() + "/log";
    }

    public String getAllowedUserEmails() {
        return allowedUserEmails;
    }

    public void setAllowedUserEmails(String allowedUserEmails) {
        this.allowedUserEmails = allowedUserEmails != null ? allowedUserEmails : "";
    }

    /** 이메일 제한이 켜져 있는지 (목록이 1개 이상일 때). */
    public boolean isEmailAllowlistEnabled() {
        return !parsedAllowedUserEmails().isEmpty();
    }

    public List<String> parsedAllowedUserEmails() {
        if (allowedUserEmails == null || allowedUserEmails.isBlank()) {
            return List.of();
        }
        return Arrays.stream(allowedUserEmails.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .toList();
    }

    public boolean isEmailAllowed(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String e = email.trim().toLowerCase(Locale.ROOT);
        return parsedAllowedUserEmails().contains(e);
    }
}

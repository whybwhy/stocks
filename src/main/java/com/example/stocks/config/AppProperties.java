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
     * 공개 price_alerts 목록·돌파 로그 경로 ({@code /private/{slug}/list}, {@code /{slug}}, {@code /{slug}/list}, {@code /{slug}/log}).
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
        return "/private/" + normalizedPriceAlertPublicSlug() + "/list";
    }

    /** 공개 단축 전체 목록 {@code /{slug}} (선행 슬래시 포함, {@code /private/{slug}/list} 와 동일 기능). */
    public String publicPriceAlertListSlugHref() {
        return "/" + normalizedPriceAlertPublicSlug();
    }

    /** {@link #publicPriceAlertListSlugHref()} 의 경로 접두만 — 페이징·폼 {@code listPathPrefix} 용. */
    public String publicPriceAlertListSlugPathWithoutLeadingSlash() {
        return normalizedPriceAlertPublicSlug();
    }

    public String publicPriceAlertLogPath() {
        return "/" + normalizedPriceAlertPublicSlug() + "/log";
    }

    /** 메모 원장(차트보이·효니효니 최신 적재일) 보기 경로 ({@code /{slug}/log/new}). */
    public String publicPriceAlertMemoLogNewPath() {
        return "/" + normalizedPriceAlertPublicSlug() + "/log/new";
    }

    /**
     * {@code true}면 공개 목록·돌파 로그 URL을 평일에 한해 {@link #priceAlertPublicWindowStartHour}~{@link #priceAlertPublicWindowEndHour}(미만) 에만 허용.
     * {@code false}면 평일은 24시간 접근 가능. 토·일 차단은 {@link #priceAlertPublicCloseOnWeekends} 와 별개.
     * {@code /stocker} 는 원래 무제한.
     */
    private boolean priceAlertPublicTimeRestricted = true;

    /**
     * {@code true}면 토·일(설정 타임존 달력)에는 공개 slug·돌파 로그 경로를 허용하지 않음.
     * 평일에는 {@link #isPriceAlertPublicTimeRestricted}·{@link #isWithinPriceAlertPublicTimeWindow} 만 적용됩니다.
     */
    private boolean priceAlertPublicCloseOnWeekends = false;

    /**
     * 타임존 (공개 목록·돌파 로그 접근 시간대). 기본 {@code Asia/Seoul}.
     */
    private String priceAlertPublicWindowZone = "Asia/Seoul";

    /**
     * 공개 접근 허용 시작 시(포함). 0~23, 기본 6 (오전 6시).
     */
    private int priceAlertPublicWindowStartHour = 6;

    /**
     * 공개 접근 종료 시(미포함). 0~24, 기본 10 → 10시 정각부터 차단 (= 06:00~09:59:59 허용).
     */
    private int priceAlertPublicWindowEndHour = 10;

    public String getPriceAlertPublicWindowZone() {
        return priceAlertPublicWindowZone != null ? priceAlertPublicWindowZone : "Asia/Seoul";
    }

    public void setPriceAlertPublicWindowZone(String priceAlertPublicWindowZone) {
        this.priceAlertPublicWindowZone = priceAlertPublicWindowZone;
    }

    public int getPriceAlertPublicWindowStartHour() {
        return clampHour(priceAlertPublicWindowStartHour, 6);
    }

    public void setPriceAlertPublicWindowStartHour(int priceAlertPublicWindowStartHour) {
        this.priceAlertPublicWindowStartHour = priceAlertPublicWindowStartHour;
    }

    public int getPriceAlertPublicWindowEndHour() {
        int end = clampHour(priceAlertPublicWindowEndHour, 10);
        int start = getPriceAlertPublicWindowStartHour();
        if (end <= start && start < 23) {
            end = Math.min(23, start + 4);
        }
        if (end <= start) {
            return 23;
        }
        return end;
    }

    public void setPriceAlertPublicWindowEndHour(int priceAlertPublicWindowEndHour) {
        this.priceAlertPublicWindowEndHour = priceAlertPublicWindowEndHour;
    }

    public boolean isPriceAlertPublicTimeRestricted() {
        return priceAlertPublicTimeRestricted;
    }

    public void setPriceAlertPublicTimeRestricted(boolean priceAlertPublicTimeRestricted) {
        this.priceAlertPublicTimeRestricted = priceAlertPublicTimeRestricted;
    }

    public boolean isPriceAlertPublicCloseOnWeekends() {
        return priceAlertPublicCloseOnWeekends;
    }

    public void setPriceAlertPublicCloseOnWeekends(boolean priceAlertPublicCloseOnWeekends) {
        this.priceAlertPublicCloseOnWeekends = priceAlertPublicCloseOnWeekends;
    }

    /**
     * 설정 타임존 기준 토요일 또는 일요일 여부와 {@link #priceAlertPublicCloseOnWeekends} 조합 —
     * 공개 경로 차단 필터 전용 ({@code true} 면 접근 차단 필요).
     */
    public boolean isPriceAlertBlockedForWeekendNow(java.time.ZonedDateTime now) {
        if (!priceAlertPublicCloseOnWeekends) {
            return false;
        }
        java.time.DayOfWeek dow = now.getDayOfWeek();
        return dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY;
    }

    private static int clampHour(int h, int d) {
        if (h < 0 || h > 23) {
            return d;
        }
        return h;
    }

    /** 현재 시각이 {@link #priceAlertPublicWindowStartHour} 이상 · {@link #priceAlertPublicWindowEndHour} 미만 인지 (설정 타임존 기준 시계). */
    public boolean isWithinPriceAlertPublicTimeWindow(java.time.ZonedDateTime now) {
        java.time.LocalTime t = now.toLocalTime();
        java.time.LocalTime start = java.time.LocalTime.of(getPriceAlertPublicWindowStartHour(), 0);
        java.time.LocalTime end = java.time.LocalTime.of(getPriceAlertPublicWindowEndHour(), 0);
        return !t.isBefore(start) && t.isBefore(end);
    }

    /** 공개 시간대 검사용 {@link java.time.ZonedDateTime}. */
    public java.time.ZonedDateTime nowInPriceAlertPublicWindowZone() {
        try {
            return java.time.ZonedDateTime.now(java.time.ZoneId.of(getPriceAlertPublicWindowZone()));
        } catch (java.time.DateTimeException e) {
            return java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Seoul"));
        }
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

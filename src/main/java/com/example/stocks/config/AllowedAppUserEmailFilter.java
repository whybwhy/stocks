package com.example.stocks.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * {@code app.allowed-user-emails} 가 비어 있지 않을 때, 카카오 OAuth2 로그인 사용자의 이메일이
 * 목록에 없으면 접근을 거부합니다. {@code /private/{slug}/list}·{@code /{slug}}·{@code /{slug}/list}·{@code /{slug}/log}·로그인·로그아웃·헬스 등은 제외합니다.
 */
public class AllowedAppUserEmailFilter extends OncePerRequestFilter {

    private final AppProperties appProperties;
    private final ChartboyPublicAccess chartboyPublicAccess;

    public AllowedAppUserEmailFilter(AppProperties appProperties, ChartboyPublicAccess chartboyPublicAccess) {
        this.appProperties = appProperties;
        this.chartboyPublicAccess = chartboyPublicAccess;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!appProperties.isEmailAllowlistEnabled() || isExemptPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof OAuth2User oauth2User)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = resolveKakaoEmail(oauth2User);
        if (appProperties.isEmailAllowed(email)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/?error=forbidden");
    }

    private boolean isExemptPath(HttpServletRequest request) {
        if (chartboyPublicAccess.isPriceAlertPublicPath(request)) {
            return true;
        }
        String path = ChartboyPublicAccess.normalizedDispatchPath(request);
        if ("/".equals(path) || "/index".equals(path) || "/health".equals(path) || "/favicon.png".equals(path)) {
            return true;
        }
        if (path.startsWith("/oauth2/") || path.startsWith("/login/oauth2/")) {
            return true;
        }
        if ("/logout".equals(path)) {
            return true;
        }
        if ("/error".equals(path)) {
            return true;
        }
        return false;
    }

    private static String resolveKakaoEmail(OAuth2User user) {
        Object account = user.getAttributes().get("kakao_account");
        if (account instanceof Map<?, ?> map) {
            Object email = map.get("email");
            return email != null && !email.toString().isBlank() ? email.toString().trim() : null;
        }
        return null;
    }
}

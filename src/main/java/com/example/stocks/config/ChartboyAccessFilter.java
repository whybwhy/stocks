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
import java.util.List;
import java.util.Map;

/**
 * /stock/chartboy( 및 하위 경로) 접근 시 application.yml 에 등록된 카카오 계정(닉네임)만 허용.
 * /admin/stock/* 는 체크하지 않음.
 */
public class ChartboyAccessFilter extends OncePerRequestFilter {

    private final AppProperties appProperties;

    public ChartboyAccessFilter(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        // /stock/chartboy, /stock/chartboy/, /stock/chartboy/edit 등만 검사. /admin/stock/chartboy 제외
        if (!path.startsWith("/stock/chartboy")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (path.startsWith("/admin/")) {
            filterChain.doFilter(request, response);
            return;
        }

        List<String> allowed = appProperties.getAllowedKakaoAccounts();
        if (allowed == null || allowed.isEmpty()) {
            // 목록 비어 있으면 기존 동작: 모든 인증 사용자 허용
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof OAuth2User)) {
            // 인증 안 됨 → 기존 인증 필터에서 처리
            filterChain.doFilter(request, response);
            return;
        }

        String nickname = resolveNickname((OAuth2User) auth.getPrincipal());
        boolean allowedNickname = nickname != null && allowed.stream()
                .anyMatch(a -> a != null && a.trim().equalsIgnoreCase(nickname.trim()));

        if (!allowedNickname) {
            response.sendRedirect(request.getContextPath() + "/?error=forbidden");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static String resolveNickname(OAuth2User user) {
        Object props = user.getAttributes().get("properties");
        if (props instanceof Map<?, ?> map) {
            Object n = map.get("nickname");
            return n != null && !n.toString().isBlank() ? n.toString() : null;
        }
        return null;
    }
}

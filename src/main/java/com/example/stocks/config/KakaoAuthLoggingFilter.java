package com.example.stocks.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 요청마다 카카오(또는 OAuth2) 인증 정보를 로그로 출력합니다.
 */
public class KakaoAuthLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(KakaoAuthLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof OAuth2User) {
            OAuth2User user = (OAuth2User) auth.getPrincipal();
            String name = user.getName();
            Map<String, Object> attrs = user.getAttributes();
            String attrsStr = attrs.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(", "));
            log.info("[Kakao 인증] principal={}, name={}, attributes=[{}]", auth.getPrincipal().getClass().getSimpleName(), name, attrsStr);
        } else if (auth != null && !auth.isAuthenticated()) {
            log.debug("[인증] 미인증 principal={}", auth.getPrincipal());
        }

        filterChain.doFilter(request, response);
    }
}

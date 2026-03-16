package com.example.stocks.config;

import com.example.stocks.supabase.SupabaseService;
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
 * /stock/{service}( 및 하위 경로) 접근 시 service_permission 테이블로 권한 검사.
 * 해당 service에 권한 행이 0건이면 전체 허용, 1건 이상이면 등록된 nickname만 허용.
 * /admin/stock/* 는 검사하지 않음.
 */
public class ServicePermissionFilter extends OncePerRequestFilter {

    private final SupabaseService supabaseService;

    public ServicePermissionFilter(SupabaseService supabaseService) {
        this.supabaseService = supabaseService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/admin/")) {
            filterChain.doFilter(request, response);
            return;
        }
        // /stock/xxx 또는 /stock/xxx/yyy 형태에서 service 추출
        if (!path.startsWith("/stock/")) {
            filterChain.doFilter(request, response);
            return;
        }
        String[] segments = path.split("/");
        // ["", "stock", "chartboy"] or ["", "stock", "chartboy", "edit"]
        if (segments.length < 3 || segments[2].isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }
        String service = segments[2];

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof OAuth2User)) {
            filterChain.doFilter(request, response);
            return;
        }

        String nickname = resolveNickname((OAuth2User) auth.getPrincipal());
        if (!supabaseService.hasAccess(nickname, service)) {
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

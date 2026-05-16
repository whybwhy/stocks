package com.example.stocks.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * {@code app.price-alert-public-slug} 기준 목록·돌파 로그·오타 로그 리다이렉트 등
 * 시간대 외 접근 시 403. 주말 차단 옵션({@code app.price-alert-public-close-on-weekends})이 켜지면 토·일에도 동일 영역만 403.
 * {@code /stocker} 등은 {@link ChartboyPublicAccess} 와 별도로 제외.
 */
@Component
public class PublicPriceAlertTimeWindowFilter extends OncePerRequestFilter {

    private final AppProperties appProperties;
    private final ChartboyPublicAccess chartboyPublicAccess;

    public PublicPriceAlertTimeWindowFilter(AppProperties appProperties, ChartboyPublicAccess chartboyPublicAccess) {
        this.appProperties = appProperties;
        this.chartboyPublicAccess = chartboyPublicAccess;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!chartboyPublicAccess.isPriceAlertSlugScopedPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        var now = appProperties.nowInPriceAlertPublicWindowZone();
        if (appProperties.isPriceAlertBlockedForWeekendNow(now)) {
            writeWeekendClosed(response, appProperties.getPriceAlertPublicWindowZone());
            return;
        }
        if (!appProperties.isPriceAlertPublicTimeRestricted()) {
            filterChain.doFilter(request, response);
            return;
        }
        if (appProperties.isWithinPriceAlertPublicTimeWindow(now)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setContentType(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8");
        String z = escapeHtml(appProperties.getPriceAlertPublicWindowZone());
        int sh = appProperties.getPriceAlertPublicWindowStartHour();
        int eh = appProperties.getPriceAlertPublicWindowEndHour();
        String body = """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width,initial-scale=1"/>
                  <title>접근 불가</title>
                </head>
                <body style="font-family:system-ui,sans-serif;padding:1.5rem;max-width:28rem;margin:2rem auto;">
                  <p style="font-weight:600;">공개 목표가 페이지는 접속 가능 시간 외에는 열람할 수 없습니다.</p>
                  <p>%02d시 ~ %02d시 (%s 기준 오전 시간대).</p>
                </body>
                </html>
                """.formatted(sh, eh, z);
        response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
    }

    private void writeWeekendClosed(HttpServletResponse response, String zoneIdRaw) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setContentType(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8");
        String z = escapeHtml(zoneIdRaw);
        String body = """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width,initial-scale=1"/>
                  <title>접근 불가</title>
                </head>
                <body style="font-family:system-ui,sans-serif;padding:1.5rem;max-width:28rem;margin:2rem auto;">
                  <p style="font-weight:600;">주말에는 공개 목표가 페이지가 제공되지 않습니다.</p>
                  <p>토요일·일요일 (%s 기준).</p>
                </body>
                </html>
                """.formatted(z);
        response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
    }

    private static String escapeHtml(String raw) {
        if (raw == null) return "";
        return raw.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}

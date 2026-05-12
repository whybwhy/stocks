package com.example.stocks.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * 공개 price_alerts URL ({@code /{app.price-alert-public-slug}/list|log}) 판별.
 */
@Component
public class ChartboyPublicAccess {

    private final AppProperties appProperties;

    public ChartboyPublicAccess(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public boolean isPriceAlertPublicPath(HttpServletRequest request) {
        String path = normalizedDispatchPath(request);
        String slug = appProperties.normalizedPriceAlertPublicSlug();
        if (pathEqualsOrChild(path, "/" + slug + "/list")) {
            return true;
        }
        if (pathEqualsOrChild(path, "/" + slug + "/log")) {
            return true;
        }
        if (pathEqualsOrChild(path, "/stocker")) {
            return true;
        }
        return "/chartbody/log".equals(path);
    }

    private static boolean pathEqualsOrChild(String path, String base) {
        return base.equals(path) || path.startsWith(base + "/");
    }

    public static String normalizedDispatchPath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (servletPath != null && !servletPath.isEmpty()) {
            String pathInfo = request.getPathInfo();
            String p = pathInfo != null ? servletPath + pathInfo : servletPath;
            return p.isEmpty() ? "/" : p;
        }
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            uri = uri.substring(ctx.length());
        }
        return uri == null || uri.isEmpty() ? "/" : uri;
    }
}

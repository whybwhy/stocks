package com.example.stocks.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * 공개 price_alerts URL ({@code /private/{slug}/list|…}, {@code /{slug}|…}, {@code /{slug}/list|…}, {@code /{slug}/log}) 판별.
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
        if (pathEqualsOrChild(path, "/private/" + slug + "/list")) {
            return true;
        }
        if (slugRootListingOrSuggest(path, slug)) {
            return true;
        }
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

    /**
     * 시간대 제한 대상 ({@code /private/{slug}/list|…}, {@code /{slug}|…}, {@code /{slug}/list|…}, {@code /{slug}/log|…}, {@code /chartbody/log}).
     * {@code /stocker} 등 다른 공개 경로는 제외.
     */
    public boolean isPriceAlertSlugScopedPath(HttpServletRequest request) {
        String path = normalizedDispatchPath(request);
        String slug = appProperties.normalizedPriceAlertPublicSlug();
        if ("/chartbody/log".equals(path)) {
            return true;
        }
        if (pathEqualsOrChild(path, "/private/" + slug + "/list")) {
            return true;
        }
        if (slugRootListingOrSuggest(path, slug)) {
            return true;
        }
        if (pathEqualsOrChild(path, "/" + slug + "/list")) {
            return true;
        }
        return pathEqualsOrChild(path, "/" + slug + "/log");
    }

    private static boolean slugRootListingOrSuggest(String path, String slug) {
        String root = "/" + slug;
        return root.equals(path) || (root + "/suggest").equals(path);
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

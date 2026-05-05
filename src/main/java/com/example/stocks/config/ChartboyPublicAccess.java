package com.example.stocks.config;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 차트보이 가격 알람 공개 URL (비로그인). 보안·이메일 필터에서 동일 판별을 씁니다.
 */
public final class ChartboyPublicAccess {

    private ChartboyPublicAccess() {
    }

    public static boolean isPriceAlertPublicPath(HttpServletRequest request) {
        String path = normalizedDispatchPath(request);
        if ("/chartboy/list".equals(path) || path.startsWith("/chartboy/list/")) {
            return true;
        }
        if ("/chartboy/log".equals(path) || path.startsWith("/chartboy/log/")) {
            return true;
        }
        return "/chartbody/log".equals(path);
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

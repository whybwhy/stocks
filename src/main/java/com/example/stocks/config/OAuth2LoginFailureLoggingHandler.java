package com.example.stocks.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

/**
 * 카카오(OAuth2) 로그인 실패 시 예외를 로그로 남긴 뒤 기존 실패 URL로 리다이렉트합니다.
 */
public class OAuth2LoginFailureLoggingHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginFailureLoggingHandler.class);

    public OAuth2LoginFailureLoggingHandler(String defaultFailureUrl) {
        super(defaultFailureUrl);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String remoteAddr = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        log.warn("[OAuth2 로그인 실패] remoteAddr={}, userAgent={}, message={}, cause={}",
                remoteAddr,
                userAgent != null ? userAgent : "",
                exception.getMessage(),
                exception.getCause() != null ? exception.getCause().getMessage() : null);

        String msg = exception.getMessage() != null ? exception.getMessage() : "";
        if (msg.contains("KOE237") || msg.contains("rate limit exceeded")) {
            setDefaultFailureUrl("/?error=rate_limit");
        }
        super.onAuthenticationFailure(request, response, exception);
    }
}

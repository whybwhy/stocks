package com.example.stocks.config;

import com.example.stocks.supabase.SupabaseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
public class SecurityConfig {

    private final SupabaseService supabaseService;
    private final AppProperties appProperties;
    private final ChartboyPublicAccess chartboyPublicAccess;
    private final PublicPriceAlertTimeWindowFilter publicPriceAlertTimeWindowFilter;

    public SecurityConfig(SupabaseService supabaseService, AppProperties appProperties,
                          ChartboyPublicAccess chartboyPublicAccess,
                          PublicPriceAlertTimeWindowFilter publicPriceAlertTimeWindowFilter) {
        this.supabaseService = supabaseService;
        this.appProperties = appProperties;
        this.chartboyPublicAccess = chartboyPublicAccess;
        this.publicPriceAlertTimeWindowFilter = publicPriceAlertTimeWindowFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler =
                new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setDefaultTargetUrl("/main");
        successHandler.setAlwaysUseDefaultTargetUrl(false);

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index", "/health", "/favicon.png", "/chartboy-logo.webp").permitAll()
                        .requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()
                        // 차트보이 공개 목록·돌파 로그: 문자열 패턴이 서블릿 경로와 어긋나면 OAuth로 튕기므로 RequestMatcher로 명시
                        .requestMatchers(chartboyPublicAccess::isPriceAlertPublicPath).permitAll()
                        .requestMatchers("/admin", "/admin/**").authenticated()
                        .requestMatchers("/api/alerts", "/api/alerts/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/")
                        .failureHandler(new OAuth2LoginFailureLoggingHandler("/?error"))
                        .successHandler(successHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .addFilterBefore(new KakaoAuthLoggingFilter(), AuthorizationFilter.class)
                .addFilterBefore(publicPriceAlertTimeWindowFilter, AuthorizationFilter.class)
                .addFilterBefore(new AllowedAppUserEmailFilter(appProperties, chartboyPublicAccess), AuthorizationFilter.class)
                .addFilterBefore(new ServicePermissionFilter(supabaseService), AuthorizationFilter.class);

        return http.build();
    }
}


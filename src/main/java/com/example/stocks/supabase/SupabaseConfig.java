package com.example.stocks.supabase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties(SupabaseProperties.class)
public class SupabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(SupabaseConfig.class);

    @Bean
    public RestClient supabaseRestClient(SupabaseProperties properties) {
        ClientHttpRequestInterceptor loggingInterceptor = (request, body, execution) -> {
            log.info("Supabase request: method={} uri={} headers={} body={}",
                    request.getMethod(), request.getURI(), request.getHeaders(),
                    new String(body, StandardCharsets.UTF_8));

            ClientHttpResponse response = execution.execute(request, body);

            try {
                log.info("Supabase response: status={} headers={}",
                        response.getStatusCode(), response.getHeaders());
            } catch (IOException e) {
                log.warn("Failed to log Supabase response", e);
            }

            return response;
        };

        return RestClient.builder()
                .baseUrl(properties.url())
                .defaultHeader("apikey", properties.anonKey())
                .defaultHeader("Authorization", "Bearer " + properties.anonKey())
                .requestInterceptor(loggingInterceptor)
                .build();
    }
}


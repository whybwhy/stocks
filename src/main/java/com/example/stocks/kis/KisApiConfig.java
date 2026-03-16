package com.example.stocks.kis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@Configuration
@EnableConfigurationProperties(KisApiProperties.class)
public class KisApiConfig {

    private static final Logger log = LoggerFactory.getLogger(KisApiConfig.class);

    @Bean
    public RestClient kisRestClient(KisApiProperties properties) {
        ClientHttpRequestInterceptor loggingInterceptor = (request, body, execution) -> {
            String uri = request.getURI().toString();
            log.info("[KIS 통신] request method={} uri={} bodyLength={}", request.getMethod(), uri, body.length);

            ClientHttpResponse response = execution.execute(request, body);

            try {
                log.info("[KIS 통신] response status={} uri={}", response.getStatusCode(), uri);
            } catch (IOException e) {
                log.warn("[KIS 통신] response log failed: {}", e.getMessage());
            }
            return response;
        };

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Content-Type", "application/json")
                .requestInterceptor(loggingInterceptor)
                .build();
    }
}

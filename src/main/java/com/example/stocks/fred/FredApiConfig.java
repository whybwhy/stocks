package com.example.stocks.fred;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(FredApiProperties.class)
public class FredApiConfig {

    @Bean
    public RestClient fredRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.stlouisfed.org")
                .build();
    }
}

package com.example.stocks.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * add-mappings=false 로 기본 정적 리소스 비활성화 후, 필요한 경로만 노출.
 * favicon 등 static 폴더 내 자원은 아래 경로로만 제공 (나머지는 404 페이지로 처리).
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/favicon.png")
                .addResourceLocations("classpath:/static/");
    }
}

package com.cravelog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // /api/ 하위 모든 경로에 대해
                .allowedOrigins("http://localhost:3000", "http://localhost:5173", "https://cravelog.me") // 리액트 앱 도메인 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*")
                .allowCredentials(true) // 쿠키/인증 정보 포함 허용
                .maxAge(36000); // 프리플라이트 캐싱 시간 (10시간)
    }
}
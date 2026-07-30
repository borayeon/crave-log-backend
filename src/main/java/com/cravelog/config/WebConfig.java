package com.cravelog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
// import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ⛔️ SecurityConfig와 충돌하므로 WebConfig의 CORS 설정은 삭제합니다.
    /*
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        ...
    }
    */
}
package com.cravelog.config;

import com.cravelog.domain.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
            // 기존 4명의 가짜 더미 데이터(Mock Data) 자동 생성 로직을 제거했습니다.
            // (추후 시스템에 꼭 필요한 기본 카테고리나 기본 권한 등을 초기화할 때 이 곳을 활용하시면 됩니다.)
        };
    }
}docker run cloudflare/cloudflared:latest tunnel --no-autoupdate run --token
package com.cravelog.config;

import com.cravelog.domain.user.User;
import com.cravelog.domain.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
            // DB에 유저가 한 명도 없을 때, 연동 테스트를 위한 기초 데이터를 자동 생성합니다.
            if (userRepository.count() == 0) {
                User dummyUser = User.builder()
                        .name("손님")
                        .handle("taekyeong.dev") // 프론트엔드가 요청하는 핸들 URL
                        .email("test@cravelog.com")
                        .build();

                userRepository.save(dummyUser);
            }
        };
    }
}
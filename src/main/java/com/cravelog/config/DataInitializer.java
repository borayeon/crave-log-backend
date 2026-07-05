package com.cravelog.config;

import com.cravelog.domain.user.User;
import com.cravelog.domain.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
            // DB에 유저가 한 명도 없을 때, 연동 테스트를 위한 가짜 데이터를 자동 생성합니다.
            if (userRepository.count() == 0) {
                User dummyUser1 = User.builder()
                        .name("손님") // 본인 프로필
                        .handle("taekyeong.dev")
                        .email("test@cravelog.com")
                        .build();

                User dummyUser2 = User.builder()
                        .name("김개발")
                        .handle("kim.dev")
                        .email("kim@cravelog.com")
                        .build();
                dummyUser2.updateProfile(
                        "김개발", "https://api.dicebear.com/7.x/avataaars/svg?seed=kim", "Backend Engineer", "컴퓨터공학", "Seoul, Korea",
                        "자바를 사랑하는 백엔드 개발자입니다.", "커피 수혈 중 ☕️", List.of("Java", "Spring", "Backend"), List.of("토이 프로젝트 완성하기"),
                        null, null, null, Map.of("developer", true, "career", true, "idol", true)
                );

                User dummyUser3 = User.builder()
                        .name("이지은")
                        .handle("jieun.lee")
                        .email("jieun@cravelog.com")
                        .build();
                dummyUser3.updateProfile(
                        "이지은", "https://api.dicebear.com/7.x/avataaars/svg?seed=jieun", "UX/UI Designer", "산업디자인", "Busan, Korea",
                        "사용자 중심의 디자인을 고민합니다.", "포트폴리오 작업 중 🎨", List.of("Figma", "UI", "UX"), List.of("Awwwards 메인 가기"),
                        null, null, null, Map.of("developer", false, "career", true, "idol", true)
                );

                User dummyUser4 = User.builder()
                        .name("박데이터")
                        .handle("data.park")
                        .email("park@cravelog.com")
                        .build();
                dummyUser4.updateProfile(
                        "박데이터", "https://api.dicebear.com/7.x/avataaars/svg?seed=park", "Data Analyst", "통계학", "Jeju, Korea",
                        "데이터 속에서 인사이트를 찾습니다.", "캐글 대회 준비 중 📊", List.of("Python", "SQL", "Data"), List.of("데이터 분석가 취업"),
                        null, null, null, Map.of("developer", true, "career", true, "idol", true)
                );

                userRepository.saveAll(List.of(dummyUser1, dummyUser2, dummyUser3, dummyUser4));
            }
        };
    }
}
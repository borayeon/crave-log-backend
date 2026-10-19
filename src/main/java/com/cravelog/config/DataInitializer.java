package com.cravelog.config;

import com.cravelog.domain.user.User;
import com.cravelog.domain.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Configuration
public class DataInitializer {

    @Value("${ADMIN_INITIAL_PASSWORD:}")
    private String adminInitialPassword;

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // ⭐️ 시스템 최고 관리자(Admin) 전용 독립 계정 생성 및 권한 검증 (하드코딩된 평문 비밀번호 제거)
            userRepository.findByHandle("admin_craver").ifPresentOrElse(
                admin -> {
                    if (!admin.isAdmin()) {
                        admin.promoteToAdmin();
                        userRepository.save(admin);
                        log.info("🛡️ 기존 admin_craver 계정에 ROLE_ADMIN 권한을 성공적으로 갱신했습니다.");
                    }
                },
                () -> {
                    String initialSecret = (adminInitialPassword != null && !adminInitialPassword.isBlank())
                            ? adminInitialPassword
                            : generateSecureRandomPassword();

                    User admin = User.builder()
                            .name("CraveLog 최고관리자")
                            .handle("admin_craver")
                            .email("admin@cravelog.local")
                            .password(passwordEncoder.encode(initialSecret))
                            .oauthProvider("LOCAL")
                            .oauthId("admin_craver")
                            .build();

                    admin.promoteToAdmin(); // ROLE_ADMIN 부여
                    userRepository.save(admin);
                    log.info("🛡️ 시스템 최고 관리자 계정(admin_craver / ROLE_ADMIN)이 초기화되었습니다.");
                }
            );

            // ⭐️ 실사용 계정(taeya00)이 관리자로 잘못 설정되어 있지 않도록 ROLE_USER 권한 보장
            userRepository.findByHandle("taeya00").ifPresent(user -> {
                if (user.isAdmin()) {
                    user.demoteToUser();
                    userRepository.save(user);
                    log.info("👤 실사용 계정(taeya00)의 권한을 일반 유저(ROLE_USER)로 정상 설정했습니다.");
                }
            });
        };
    }

    private String generateSecureRandomPassword() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
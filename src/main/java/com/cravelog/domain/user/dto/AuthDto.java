package com.cravelog.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AuthDto {

    @Getter @Setter
    @NoArgsConstructor
    public static class SignupRequest {
        private String email;
        private String password;
        private String name;
        private String handle;
    }

    @Getter
    @NoArgsConstructor
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Getter
    @AllArgsConstructor
    public static class TokenResponse {
        private String token;
    }

    // ⭐️ 인증번호 전송 요청
    @Getter
    @NoArgsConstructor
    public static class EmailSendRequest {
        private String email;
    }

    // ⭐️ 인증번호 검증 요청
    @Getter
    @NoArgsConstructor
    public static class EmailVerifyRequest {
        private String email;
        private String code;
    }

    // ⭐️ 인증된 이메일 기반 비밀번호 재설정 요청 (수정됨)
    @Getter
    @NoArgsConstructor
    public static class ResetPasswordRequest {
        private String email;
        private String code; // 2차 검증용
        private String newPassword;
    }
}
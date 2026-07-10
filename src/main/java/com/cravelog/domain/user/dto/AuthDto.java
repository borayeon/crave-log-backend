package com.cravelog.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AuthDto {

    // ⭐️ 회원가입 요청 DTO (handle 추가)
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

    // ⭐️ 아이디 찾기 요청
    @Getter
    @NoArgsConstructor
    public static class FindIdRequest {
        private String email;
        private String name;
    }

    // ⭐️ 아이디 찾기 응답
    @Getter
    @AllArgsConstructor
    public static class FindIdResponse {
        private String handle;
    }

    // ⭐️ 비밀번호 재설정 요청
    @Getter
    @NoArgsConstructor
    public static class ResetPasswordRequest {
        private String email;
        private String name;
        private String newPassword;
    }
}
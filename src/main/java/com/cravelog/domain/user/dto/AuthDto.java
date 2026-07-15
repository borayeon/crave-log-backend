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

    @Getter @Setter
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

    @Getter @Setter
    @NoArgsConstructor
    public static class FindIdRequest {
        private String email;
        private String name;
    }

    @Getter
    @AllArgsConstructor
    public static class FindIdResponse {
        private String handle;
    }

    // ⭐️ 수정: @Setter 추가 (JSON 데이터 바인딩 오류 방지)
    @Getter @Setter
    @NoArgsConstructor
    public static class EmailSendRequest {
        private String email;
    }

    // ⭐️ 수정: @Setter 추가
    @Getter @Setter
    @NoArgsConstructor
    public static class EmailVerifyRequest {
        private String email;
        private String code;
    }

    // ⭐️ 수정: @Setter 추가
    @Getter @Setter
    @NoArgsConstructor
    public static class ResetPasswordRequest {
        private String email;
        private String code;
        private String newPassword;
    }
}
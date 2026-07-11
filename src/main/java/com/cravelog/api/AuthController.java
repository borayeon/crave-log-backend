package com.cravelog.api;

import com.cravelog.domain.user.AuthService;
import com.cravelog.domain.user.dto.AuthDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody AuthDto.SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto.TokenResponse> login(@RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = authService.checkEmailExists(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    // ⭐️ 비밀번호 찾기 - 1. 인증번호 발송 요청
    @PostMapping("/password/email/send")
    public ResponseEntity<Void> sendPasswordResetEmail(@RequestBody AuthDto.EmailSendRequest request) {
        authService.sendPasswordResetEmail(request.getEmail());
        return ResponseEntity.ok().build();
    }

    // ⭐️ 비밀번호 찾기 - 2. 인증번호 검증 요청
    @PostMapping("/password/email/verify")
    public ResponseEntity<Map<String, Boolean>> verifyEmailCode(@RequestBody AuthDto.EmailVerifyRequest request) {
        boolean isValid = authService.verifyEmailCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(Map.of("isValid", isValid));
    }

    // ⭐️ 비밀번호 찾기 - 3. 새 비밀번호 재설정 처리
    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@RequestBody AuthDto.ResetPasswordRequest request) {
        authService.resetPasswordWithCode(request);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}
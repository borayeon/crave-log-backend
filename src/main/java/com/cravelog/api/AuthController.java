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

    // 회원가입 API
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody AuthDto.SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok().build();
    }

    // 로그인 API
    @PostMapping("/login")
    public ResponseEntity<AuthDto.TokenResponse> login(@RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // ⭐️ 아이디 찾기 API
    @PostMapping("/find-id")
    public ResponseEntity<AuthDto.FindIdResponse> findId(@RequestBody AuthDto.FindIdRequest request) {
        return ResponseEntity.ok(authService.findId(request));
    }

    // ⭐️ 비밀번호 재설정 API
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody AuthDto.ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    // 예외 처리 (에러 메시지를 프론트엔드로 반환)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    // ⭐️ 이메일 존재 여부 확인 API (2-Step 로그인을 위함)
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = authService.checkEmailExists(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}
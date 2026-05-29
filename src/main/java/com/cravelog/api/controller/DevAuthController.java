package com.cravelog.api.controller;

import com.cravelog.api.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class DevAuthController {

    private final JwtProvider jwtProvider;

    /**
     * [개발용] 원하는 유저 ID로 JWT 토큰을 강제 발급합니다.
     * GET http://localhost:8081/api/v1/auth/dev-token?userId=1
     */
    @GetMapping("/dev-token")
    public Map<String, String> getDevToken(@RequestParam(defaultValue = "1") Long userId) {
        String token = jwtProvider.createToken(userId);

        Map<String, String> response = new HashMap<>();
        response.put("userId", String.valueOf(userId));
        response.put("token", token);
        response.put("prefix", "Bearer ");

        return response;
    }
}

package com.cravelog.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    // ⭐️ 쉼표로 들어올 것을 대비해 String[] 배열로 받거나, String으로 받은 뒤 자릅니다.
    @Value("${app.auth.allowed-origins:http://localhost:5173}")
    private String frontendUrls;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        System.err.println("🚨 카카오 로그인 실패 원인: " + exception.getMessage());
        exception.printStackTrace();

        // ⭐️ 핵심 수정: 쉼표(,)가 있다면 무조건 첫 번째 도메인만 추출!
        String targetUrl = frontendUrls.split(",")[0].trim();

        String errorMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);

        // 정제된 단일 도메인으로 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, targetUrl + "?error=" + errorMessage);
    }
}
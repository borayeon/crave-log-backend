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

    // ⭐️ application.yml에 등록된 프론트엔드 주소를 가져옵니다 (Render 환경변수)
    @Value("${app.auth.allowed-origins}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        System.err.println("🚨 카카오 로그인 실패 원인: " + exception.getMessage());
        exception.printStackTrace();

        String errorMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);

        // ⭐️ 고정된 localhost 대신 Vercel 도메인으로 리다이렉트합니다.
        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "?error=" + errorMessage);
    }
}
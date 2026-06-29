package com.cravelog.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        // 서버 콘솔에 정확한 에러 원인을 출력합니다.
        System.err.println("🚨 카카오 로그인 실패 원인: " + exception.getMessage());
        exception.printStackTrace();

        // 에러 메시지를 프론트엔드로 전달하기 위해 인코딩
        String errorMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);

        // 프론트엔드(React) 주소로 리다이렉트 (없는 /login으로 가지 않게 막음)
        getRedirectStrategy().sendRedirect(request, response, "http://localhost:5173?error=" + errorMessage);
    }
}
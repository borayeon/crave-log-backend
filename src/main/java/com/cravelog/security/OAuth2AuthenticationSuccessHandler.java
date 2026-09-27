package com.cravelog.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;

    @Value("${app.auth.authorized-redirect-uris}")
    private String redirectUris; // ⭐️ 변수명을 복수형으로 변경하여 인지하기 쉽게 함

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 1. 유저 ID 추출 (CustomOAuth2UserService에서 세팅한 PK 값)
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Long userId = (Long) oAuth2User.getAttributes().get("id");

        // 2. JWT 토큰 생성
        String token = tokenProvider.createToken(userId);

        // ⭐️ 핵심 수정: 쉼표(,)가 포함되어 있다면 무조건 첫 번째 주소만 추출!
        String targetUri = redirectUris.split(",")[0].trim();

        // 3. 프론트엔드로 리다이렉트 (URL 쿼리 파라미터에 토큰을 실어 보냄)
        String targetUrl = UriComponentsBuilder.fromUriString(targetUri)
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
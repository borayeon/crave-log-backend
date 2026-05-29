package com.cravelog.api.auth;

import com.cravelog.api.domain.enums.Role;
import com.cravelog.api.domain.user.User;
import com.cravelog.api.domain.user.UserRepository;
import com.cravelog.api.security.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Value("${app.auth.authorized-redirect-uris}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 카카오 고유 ID (예: 3141592...)
        String kakaoId = String.valueOf(attributes.get("id"));

        // 카카오 프로필 정보 파싱 (설정에 따라 키값이 다를 수 있음)
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        String nickname = properties != null ? (String) properties.get("nickname") : "User";

        // DB에서 유저 조회, 없으면 자동 회원가입
        User user = userRepository.findByKakaoId(kakaoId).orElseGet(() -> {
            User newUser = User.builder()
                    .kakaoId(kakaoId)
                    .nickname(nickname)
                    .handle("user_" + kakaoId.substring(0, 5)) // 임시 핸들
                    .role(Role.USER)
                    .build();
            return userRepository.save(newUser);
        });

        // 찐짜 JWT 토큰 발급!
        String token = jwtProvider.createToken(user.getId());

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build().toUriString();

        if (response.isCommitted()) {
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

package com.cravelog.security;

import com.cravelog.domain.user.User;
import com.cravelog.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 1. 카카오에서 정보 추출
        String oauthId = String.valueOf(attributes.get("id"));
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String nickname = (String) profile.get("nickname");
        String email = kakaoAccount.containsKey("email") ? (String) kakaoAccount.get("email") : "no-email@kakao.com";

        // 2. DB 저장 및 업데이트
        Optional<User> userOptional = userRepository.findByOauthId(oauthId);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // 기존 유저면 정보 갱신 처리 등을 할 수 있음
        } else {
            // 새 유저 가입 처리
            user = User.builder()
                    .oauthProvider("KAKAO")
                    .oauthId(oauthId)
                    .name(nickname)
                    .email(email)
                    .handle("user_" + oauthId) // 초기 핸들값 자동 부여
                    .build();
            userRepository.save(user);
        }

        // 3. Spring Security 내부에서 사용할 유저 객체 반환 (PK인 user.getId()를 nameAttributeKey로 사용)
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("id", user.getId(), "oauthId", oauthId), // attributes
                "id" // nameAttributeKey
        );
    }
}
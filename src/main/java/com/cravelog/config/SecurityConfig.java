package com.cravelog.config;

import com.cravelog.security.CustomOAuth2UserService;
import com.cravelog.security.JwtAuthenticationFilter;
import com.cravelog.security.OAuth2AuthenticationSuccessHandler;
import com.cravelog.security.OAuth2AuthenticationFailureHandler; // ⭐️ 추가
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler; // ⭐️ 추가
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {}) // WebConfig의 CORS 설정 사용
                .csrf(csrf -> csrf.disable()) // REST API이므로 CSRF 비활성화
                // JWT를 사용하므로 세션을 사용하지 않음 (STATELESS)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/users/**").permitAll()
                        .requestMatchers("/api/v1/me/**").authenticated()
                        .anyRequest().permitAll()
                )

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // 카카오 데이터 처리
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler) // 성공 시 JWT 발급
                        .failureHandler(oAuth2AuthenticationFailureHandler) // ⭐️ 실패 시 404 방지 및 에러 전달
                )

                // 에러 핸들링 (401 에러 반환)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                );

        // 매 요청마다 JWT 토큰을 검사하는 필터를 제일 앞에 추가
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
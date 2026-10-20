package com.cravelog.config;

import com.cravelog.security.CustomOAuth2UserService;
import com.cravelog.security.JwtAuthenticationFilter;
import com.cravelog.security.OAuth2AuthenticationFailureHandler;
import com.cravelog.security.OAuth2AuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletResponse; // ⭐️ HttpServletResponse 임포트 추가
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // ⭐️ PasswordEncoder 빈을 PasswordEncoderConfig로 분리했으므로 여기서 삭제합니다.
    @Value("#{'${app.auth.allowed-origins}'.split(',')}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // REST API이므로 CSRF 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll() // 이메일 회원가입/로그인 API 누구나 접근 허용
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/users/*/follow").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/v1/users/*/follow").authenticated()
                        .requestMatchers("/api/v1/users/**").permitAll() // 퍼블릭 프로필 검색 및 조회 허용
                        .requestMatchers("/api/v1/records/*/verify-passcode").permitAll() // 비밀번호 검증 허용
                        .requestMatchers("/api/v1/analytics/track").permitAll() // 플랫폼 트래픽 추적 허용
                        .requestMatchers("/api/v1/admin/**").authenticated() // 관리자 API는 인증 필수
                        .requestMatchers("/api/v1/analytics/overview").authenticated() // 관리자 대시보드 API 인증 필수

                        // ⭐️ 1. 추가: 업로드된 사진(정적 리소스)은 누구나 볼 수 있어야 함
                        .requestMatchers("/uploads/**").permitAll()

                        // ⭐️ 2. 추가: 파일 업로드 API는 무조건 로그인(인증)한 사람만 쓸 수 있게 철벽 방어!
                        .requestMatchers("/api/v1/files/**").authenticated()

                        .requestMatchers("/api/v1/me/**").authenticated() // 내 정보 관리는 로그인 필요
                        .anyRequest().permitAll()
                )

                // OAuth2 (카카오) 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )

                // ⭐️ 핵심: 인증되지 않은 API 요청(AJAX) 시 302 리다이렉트 대신 401 에러 반환
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )

                // JWT 필터 등록
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS 설정 빈
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = new ArrayList<>();
        if (allowedOrigins != null) {
            origins.addAll(allowedOrigins);
        }
        // ⭐️ 로컬 관리자 콘솔(localhost:3000) 및 로컬 프론트엔드 환경 기본 허용
        List<String> defaultLocalOrigins = List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        );
        for (String localOrigin : defaultLocalOrigins) {
            if (!origins.contains(localOrigin)) {
                origins.add(localOrigin);
            }
        }

        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
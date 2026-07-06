package com.cravelog.domain.user;

import com.cravelog.domain.user.dto.AuthDto;
import com.cravelog.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * ⭐️ 로컬 이메일 회원가입
     */
    @Transactional
    public void signup(AuthDto.SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        if (userRepository.existsByHandle(request.getHandle())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디(핸들)입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // 비밀번호 암호화 저장
                .name(request.getName())
                .handle(request.getHandle())
                .oauthProvider("LOCAL") // 카카오가 아닌 자체 회원가입임을 명시
                .build();

        userRepository.save(user);
    }

    /**
     * ⭐️ 로컬 이메일 로그인
     */
    @Transactional(readOnly = true)
    public AuthDto.TokenResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 암호화된 비밀번호 비교 검증
        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 로그인 성공 시 JWT 토큰 발급
        String token = jwtTokenProvider.createToken(user.getId());
        return new AuthDto.TokenResponse(token);
    }
}
package com.cravelog.domain.user;

import com.cravelog.domain.tag.Category;
import com.cravelog.domain.tag.CategoryRepository;
import com.cravelog.domain.user.dto.AuthDto;
import com.cravelog.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CategoryRepository categoryRepository;
    private final EmailService emailService; // ⭐️ EmailService 주입

    private static final java.util.regex.Pattern HANDLE_PATTERN = java.util.regex.Pattern.compile("^[a-z0-9._-]{2,30}$");
    private static final java.util.regex.Pattern PASSWORD_PATTERN = java.util.regex.Pattern.compile("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*?_]).{8,}$");

    @Transactional
    public void signup(AuthDto.SignupRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank() || !request.getEmail().contains("@")) {
            throw new IllegalArgumentException("유효한 이메일 주소를 입력해주세요.");
        }
        String normalizedHandle = request.getHandle() != null ? request.getHandle().trim().toLowerCase() : "";
        if (!HANDLE_PATTERN.matcher(normalizedHandle).matches() || "admin_craver".equals(normalizedHandle)) {
            throw new IllegalArgumentException("아이디는 영문 소문자, 숫자, 마침표(.), 밑줄(_), 하이픈(-) 조합 2~30자만 가능합니다.");
        }
        if (request.getPassword() == null || !PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            throw new IllegalArgumentException("비밀번호는 영문, 숫자, 특수문자 조합 8자 이상이어야 합니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        if (userRepository.existsByHandle(normalizedHandle)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        User user = User.builder()
                .email(request.getEmail().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName() != null ? request.getName().trim() : normalizedHandle)
                .handle(normalizedHandle)
                .build();

        userRepository.save(user); // 1. 유저 먼저 저장

        // ⭐️ 2. 기본 카테고리 여러 개를 리스트로 묶어서 한 번에 저장!
        List<Category> defaultCategories = List.of(
                new Category(user, "음악"),
                new Category(user, "🔗 URL 보관함")
        );
        categoryRepository.saveAll(defaultCategories);
    }

    @Transactional(readOnly = true)
    public AuthDto.TokenResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.createToken(user.getId());
        return new AuthDto.TokenResponse(token);
    }

    @Transactional(readOnly = true)
    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // ⭐️ 비밀번호 재설정 인증번호 발송
    public void sendPasswordResetEmail(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("가입되지 않은 이메일입니다.");
        }
        emailService.sendVerificationCode(email);
    }

    // ⭐️ 인증번호 검증
    public boolean verifyEmailCode(String email, String code) {
        return emailService.verifyCode(email, code);
    }

    // ⭐️ 인증 완료 후 최종 비밀번호 재설정
    @Transactional
    public void resetPasswordWithCode(AuthDto.ResetPasswordRequest request) {
        if (request.getNewPassword() == null || !PASSWORD_PATTERN.matcher(request.getNewPassword()).matches()) {
            throw new IllegalArgumentException("새 비밀번호는 영문, 숫자, 특수문자 조합 8자 이상이어야 합니다.");
        }

        // 1. 혹시 모를 우회 접근을 막기 위해 마지막으로 코드 한 번 더 검증
        if (!emailService.verifyCode(request.getEmail(), request.getCode())) {
            throw new IllegalArgumentException("인증번호가 만료되었거나 일치하지 않습니다.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 2. 비밀번호 업데이트
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));

        // 3. 재사용 방지를 위해 Redis에서 인증번호 삭제
        emailService.deleteCode(request.getEmail());
    }
}
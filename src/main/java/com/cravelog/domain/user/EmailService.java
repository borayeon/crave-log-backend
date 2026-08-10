package com.cravelog.domain.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Slf4j // ⭐️ 에러 추적을 위한 로거 추가
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 1. 6자리 인증번호 생성 후 Redis에 저장 (수명 5분) 및 이메일 발송
     */
    @Async // ⭐️ [매우 중요] 프론트엔드 화면이 멈추지 않도록 비동기로 이메일 발송!
    public void sendVerificationCode(String toEmail) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일 주소가 비어있습니다.");
        }

        String code = generateCode();

        // Redis에 저장 (Key: AuthCode:이메일, Value: 인증번호, TTL: 5분)
        redisTemplate.opsForValue().set("AuthCode:" + toEmail, code, Duration.ofMinutes(5));

        // 이메일 발송 설정
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[CraveLog] 비밀번호 재설정 인증번호입니다.");
            message.setText("요청하신 인증번호는 [ " + code + " ] 입니다.\n보안을 위해 5분 이내에 입력해주세요.");

            mailSender.send(message);
            log.info("이메일 발송 성공: {}", toEmail);
        } catch (Exception e) {
            log.error("이메일 발송 실패: {}", toEmail, e);
            // 메일 전송이 실패했으니 Redis에 올려둔 코드도 지워주는 센스!
            redisTemplate.delete("AuthCode:" + toEmail);
        }
    }

    /**
     * 2. 사용자가 입력한 인증번호가 Redis에 저장된 값과 일치하는지 검증
     */
    public boolean verifyCode(String email, String inputCode) {
        String storedCode = redisTemplate.opsForValue().get("AuthCode:" + email);
        return storedCode != null && storedCode.equals(inputCode);
    }

    /**
     * 3. 인증이 완료되어 비밀번호를 변경한 후 Redis에서 데이터 삭제
     */
    public void deleteCode(String email) {
        redisTemplate.delete("AuthCode:" + email);
    }

    // 6자리 난수 생성기
    private String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}
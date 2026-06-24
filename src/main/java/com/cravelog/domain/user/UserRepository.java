package com.cravelog.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 공유 URL(예: @taekyeong.dev)로 사용자의 프로필을 찾기 위한 메서드
    Optional<User> findByHandle(String handle);

    // 추후 카카오 로그인을 위해 사용할 메서드
    Optional<User> findByOauthId(String oauthId);
}
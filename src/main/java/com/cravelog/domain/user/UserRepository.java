package com.cravelog.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByHandle(String handle);
    Optional<User> findByOauthId(String oauthId);

    // ⭐️ 검색 추가: 이름 또는 핸들이 키워드를 포함하는 경우 조회
    List<User> findByNameContainingIgnoreCaseOrHandleContainingIgnoreCase(String name, String handle);
}
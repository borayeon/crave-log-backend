package com.cravelog.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByHandle(String handle);
    Optional<User> findByOauthId(String oauthId);

    // ⭐️ 이메일 로그인 및 회원가입 중복 체크용 메서드 추가
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByHandle(String handle);

    // ⭐️ 아이디/비밀번호 찾기용 메서드 추가
    Optional<User> findByEmailAndName(String email, String name);

    // ⭐️ 관리자(ROLE_ADMIN) 계정은 퍼블릭 검색에서 절대 노출되지 않도록 제외 (MySQL 특수문자 이스케이프 버그 방어)
    @Query("SELECT u FROM User u WHERE u.userRole != com.cravelog.domain.user.UserRole.ROLE_ADMIN AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.handle) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<User> searchUsers(@Param("keyword") String keyword);

    // ⭐️ 일반 회원 목록 조회 (관리자 제외)
    List<User> findByUserRole(UserRole userRole);
}
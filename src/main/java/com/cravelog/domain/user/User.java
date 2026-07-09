package com.cravelog.domain.user;

import com.cravelog.domain.common.BaseTimeEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 (안전하게 PROTECTED)
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 소셜 로그인 및 식별 정보 ---
    private String oauthProvider; // 예: KAKAO, GOOGLE, LOCAL
    private String oauthId;       // 소셜 서비스에서 발급한 고유 ID
    private String email;

    // ⭐️ 로컬 로그인용 비밀번호 필드 추가
    private String password;

    @Column(unique = true, nullable = false)
    private String handle;        // 공유 URL용 고유 핸들 (예: taekyeong.dev)  // 공유 URL용 고유 핸들 (예: taekyeong.dev)

    // --- 기본 프로필 정보 (검색이나 목록 조회 시 자주 쓰이는 일반 컬럼) ---
    private String name;
    private String role;          // 예: Backend Developer
    private String major;         // 예: Computer Science
    private String location;
    private String statusMessage; // 예: CraveLog 엔진 고도화 중

    // ⭐️ 프로필 이미지 저장 컬럼 추가 (Base64 저장을 위해 LONGTEXT 사용)
    @Column(columnDefinition = "LONGTEXT")
    private String profileImageUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    // --- 복잡한 프로필 데이터 (JSON 컬럼으로 통째로 저장) ---
    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private List<String> tags;    // 기본 키워드 태그 목록

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private List<String> goals;   // 현재 목표 목록

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private Map<String, Object> developerData; // 개발자 탭 데이터 전체

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private Map<String, Object> careerData;    // 커리어 탭 데이터 전체

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private Map<String, Object> idolData;      // 아이돌 탭 데이터 전체

    // --- 프라이버시(공개/비공개) 설정 (JSON) ---
    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private Map<String, Boolean> privacySettings; // 예: {"developer": true, "career": true, "idol": false}

    @Builder
    public User(String oauthProvider, String oauthId, String email, String password, String name, String handle) {
        this.oauthProvider = oauthProvider;
        this.oauthId = oauthId;
        this.email = email;
        this.password = password;
        this.name = name;
        this.handle = handle;
    }

    // 비즈니스 로직: 프로필 업데이트 메서드
    public void updateProfile(String name, String profileImageUrl, String role, String major, String location,
                              String bio, String statusMessage, List<String> tags, List<String> goals,
                              Map<String, Object> developerData, Map<String, Object> careerData, Map<String, Object> idolData,
                              Map<String, Boolean> privacySettings) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.major = major;
        this.location = location;
        this.bio = bio;
        this.statusMessage = statusMessage;
        this.tags = tags;
        this.goals = goals;
        this.developerData = developerData;
        this.careerData = careerData;
        this.idolData = idolData;
        this.privacySettings = privacySettings;
    }

    // ⭐️ 비밀번호 재설정을 위한 메서드 추가
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
}
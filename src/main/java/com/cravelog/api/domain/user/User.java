package com.cravelog.api.domain.user;

import com.cravelog.api.domain.base.BaseTimeEntity;
import com.cravelog.api.domain.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String kakaoId; // 카카오 OAuth 고유 ID

    @Column(nullable = false)
    private String nickname;

    @Column(unique = true)
    private String handle; // @taekyeong.dev 같은 고유 핸들

    @Column(length = 500)
    private String bio;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public User(String kakaoId, String nickname, String handle, String bio, String profileImageUrl, Role role) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.handle = handle;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
    }
}
package com.cravelog.api.domain.archive;

import com.cravelog.api.domain.base.BaseTimeEntity;
import com.cravelog.api.domain.enums.SpaceType;
import com.cravelog.api.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "archive_blocks")
public class ArchiveBlock extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 기록의 주인

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String content; // 설명 또는 실제 내용

    private String url; // 링크 기반 기록일 경우 (SNS 등)

    private String type; // sns, card, link 등 클라이언트 렌더링 타입

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpaceType spaceType; // 어느 공간에 속하는지

    @Builder
    public ArchiveBlock(User user, String title, String content, String url, String type, SpaceType spaceType) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.url = url;
        this.type = type;
        this.spaceType = spaceType;
    }
}

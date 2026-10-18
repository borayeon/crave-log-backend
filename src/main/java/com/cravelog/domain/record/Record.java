package com.cravelog.domain.record;

import com.cravelog.domain.common.BaseTimeEntity;
import com.cravelog.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Record extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    private String categoryName; // 프론트 카드 좌측 상단에 표시될 라벨 (예: 캐릭터, 도서)

    private String recordDate;   // 예: 2026.06.20

    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl;     // 🔥 S3 링크 또는 길어진 Base64 이미지 데이터 저장

    @Column(columnDefinition = "TEXT")
    private String content;      // 기록의 간단한 내용(메모)

    @Column(length = 500)
    private String youtubeUrl;

    // 하위 호환성을 위해 유지 (0: 전체공개면 true)
    @Column(nullable = false)
    private boolean isPublic = true;

    // ⭐️ 4단계 공개 보안 레벨 (0: 전체공개, 1: 팔로워공개, 2: 비밀공개, 4: 비공개)
    @Column(nullable = false)
    private Integer visibilityLevel = 0;

    // ⭐️ 2단계 비밀공개용 패스코드
    @Column(length = 50)
    private String passcode;

    // 양방향 매핑: 게시글 하나에 달린 태그 매핑 리스트
    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecordTag> recordTags = new ArrayList<>();

    @Builder
    public Record(User user, String title, String categoryName, String recordDate, String imageUrl, String youtubeUrl, String content, boolean isPublic, Integer visibilityLevel, String passcode) {
        this.user = user;
        this.title = title;
        this.categoryName = categoryName;
        this.recordDate = recordDate;
        this.imageUrl = imageUrl;
        this.youtubeUrl = youtubeUrl;
        this.content = content;
        this.visibilityLevel = visibilityLevel != null ? visibilityLevel : (isPublic ? 0 : 4);
        this.passcode = passcode;
        this.isPublic = (this.visibilityLevel == 0);
    }

    public void update(String title, String categoryName, String recordDate, String imageUrl, String youtubeUrl, String content, boolean isPublic, Integer visibilityLevel, String passcode) {
        this.title = title;
        this.categoryName = categoryName;
        this.recordDate = recordDate;
        this.imageUrl = imageUrl;
        this.youtubeUrl = youtubeUrl;
        this.content = content;
        if (visibilityLevel != null) {
            this.visibilityLevel = visibilityLevel;
            this.passcode = passcode;
            this.isPublic = (this.visibilityLevel == 0);
        } else {
            this.isPublic = isPublic;
            this.visibilityLevel = isPublic ? 0 : 4;
        }
    }

    public void updateVisibility(Integer visibilityLevel, String passcode) {
        if (visibilityLevel != null) {
            this.visibilityLevel = visibilityLevel;
            this.passcode = passcode;
            this.isPublic = (visibilityLevel == 0);
        }
    }
}
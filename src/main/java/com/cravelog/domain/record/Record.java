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

    // Record.java 에 필드 추가
    @Column(length = 500)
    private String youtubeUrl;

    // 이 기록 자체를 남에게 숨길 것인지 여부
    @Column(nullable = false)
    private boolean isPublic = true;

    // 양방향 매핑: 게시글 하나에 달린 태그 매핑 리스트
    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecordTag> recordTags = new ArrayList<>();

    // ⭐️ 수정: youtubeUrl 파라미터 추가
    @Builder
    public Record(User user, String title, String categoryName, String recordDate, String imageUrl, String youtubeUrl, String content, boolean isPublic) {
        this.user = user;
        this.title = title;
        this.categoryName = categoryName;
        this.recordDate = recordDate;
        this.imageUrl = imageUrl;
        this.youtubeUrl = youtubeUrl; // ⭐️ 추가
        this.content = content;
        this.isPublic = isPublic;
    }

    // 🔥 수정: youtubeUrl 파라미터 추가
    public void update(String title, String categoryName, String recordDate, String imageUrl, String youtubeUrl, String content, boolean isPublic) {
        this.title = title;
        this.categoryName = categoryName;
        this.recordDate = recordDate;
        this.imageUrl = imageUrl;
        this.youtubeUrl = youtubeUrl; // ⭐️ 추가
        this.content = content;
        this.isPublic = isPublic;
    }
}
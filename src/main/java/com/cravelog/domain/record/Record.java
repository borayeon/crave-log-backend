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

    @Lob
    @Column(name = "image_url")
    private String imageUrl;     // 이미지 S3 링크 등

    // 이 기록 자체를 남에게 숨길 것인지 여부
    @Column(nullable = false)
    private boolean isPublic = true;

    // 양방향 매핑: 게시글 하나에 달린 태그 매핑 리스트
    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecordTag> recordTags = new ArrayList<>();

    @Builder
    public Record(User user, String title, String categoryName, String recordDate, String imageUrl, boolean isPublic) {
        this.user = user;
        this.title = title;
        this.categoryName = categoryName;
        this.recordDate = recordDate;
        this.imageUrl = imageUrl;
        this.isPublic = isPublic;
    }
}
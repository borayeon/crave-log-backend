package com.cravelog.domain.tag;

import com.cravelog.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 태그도 소유자가 있어야 함 (물론 category를 통해 알 수 있지만, 쿼리 성능을 위해 포함하기도 합니다)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name; // 예: 마블, 소설

    public Tag(User user, Category category, String name) {
        this.user = user;
        this.category = category;
        this.name = name;
    }
}
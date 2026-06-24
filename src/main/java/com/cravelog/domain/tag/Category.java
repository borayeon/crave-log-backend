package com.cravelog.domain.tag;

import com.cravelog.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 다중 사용자 환경이므로 이 카테고리가 누구의 것인지 알아야 함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name; // 예: 영화, 문학, 캐릭터

    // 양방향 매핑 (카테고리를 조회할 때 하위 태그들도 한 번에 불러오기 위함)
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tag> tags = new ArrayList<>();

    public Category(User user, String name) {
        this.user = user;
        this.name = name;
    }
}
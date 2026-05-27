package com.cravelog.api.domain.relationship;

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
@Table(name = "relationships", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"follower_id", "following_id"})
})
public class Relationship extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower; // 구독하는 사람 (보는 사람)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following; // 구독 당하는 사람 (주인)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpaceType grantedSpace; // 부여된 권한 레벨 (예: CLOSE, DEEP)

    @Builder
    public Relationship(User follower, User following, SpaceType grantedSpace) {
        this.follower = follower;
        this.following = following;
        this.grantedSpace = grantedSpace;
    }
}
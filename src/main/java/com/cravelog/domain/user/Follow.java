package com.cravelog.domain.user;

import com.cravelog.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "follows", uniqueConstraints = {
        @UniqueConstraint(name = "uk_follower_following", columnNames = {"follower_id", "following_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 팔로우를 한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    // 팔로우 대상 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    // 팔로우 상태 (신청 대기 PENDING / 수락 완료 ACCEPTED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FollowStatus status = FollowStatus.ACCEPTED;

    public Follow(User follower, User following, FollowStatus status) {
        this.follower = follower;
        this.following = following;
        this.status = (status != null) ? status : FollowStatus.PENDING;
    }

    public Follow(User follower, User following) {
        this(follower, following, FollowStatus.PENDING);
    }

    public void accept() {
        this.status = FollowStatus.ACCEPTED;
    }
}

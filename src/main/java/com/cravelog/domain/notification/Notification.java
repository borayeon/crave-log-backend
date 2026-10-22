package com.cravelog.domain.notification;

import com.cravelog.domain.common.BaseTimeEntity;
import com.cravelog.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 알림 수신 대상자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    // 알림 유발자 (예: 팔로우를 한 사용자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(nullable = false)
    private boolean isRead;

    @Builder
    public Notification(User recipient, User actor, NotificationType type, String message) {
        this.recipient = recipient;
        this.actor = actor;
        this.type = type;
        this.message = message;
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}

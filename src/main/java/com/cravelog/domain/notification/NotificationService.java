package com.cravelog.domain.notification;

import com.cravelog.domain.notification.dto.NotificationDto;
import com.cravelog.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createFollowNotification(User recipient, User actor) {
        createFollowRequestNotification(recipient, actor);
    }

    @Transactional
    public void createFollowRequestNotification(User recipient, User actor) {
        if (recipient.getId().equals(actor.getId())) {
            return;
        }

        String actorDisplayName = (actor.getName() != null && !actor.getName().isBlank())
                ? actor.getName()
                : "@" + actor.getHandle();

        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(NotificationType.FOLLOW_REQUEST)
                .message(actorDisplayName + "님이 팔로우를 신청했습니다.")
                .build();

        notificationRepository.save(notification);
    }

    @Transactional
    public void createFollowAcceptNotification(User recipient, User actor) {
        if (recipient.getId().equals(actor.getId())) {
            return;
        }

        String actorDisplayName = (actor.getName() != null && !actor.getName().isBlank())
                ? actor.getName()
                : "@" + actor.getHandle();

        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(NotificationType.FOLLOW_ACCEPT)
                .message(actorDisplayName + "님이 회원님의 팔로우 신청을 수락했습니다.")
                .build();

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto.Response> getMyNotifications(Long userId) {
        return notificationRepository.findAllByRecipientIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationDto.Response::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByRecipientId(userId);
    }

    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteAllForUser(Long userId) {
        notificationRepository.deleteAllByUserId(userId);
    }
}

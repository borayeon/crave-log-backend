package com.cravelog.api;

import com.cravelog.domain.notification.NotificationService;
import com.cravelog.domain.notification.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 내 알림 목록 조회 (최신순)
     */
    @GetMapping
    public ResponseEntity<List<NotificationDto.Response>> getMyNotifications(
            @AuthenticationPrincipal User principal
    ) {
        Long myUserId = Long.parseLong(principal.getUsername());
        return ResponseEntity.ok(notificationService.getMyNotifications(myUserId));
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    @GetMapping("/unread-count")
    public ResponseEntity<NotificationDto.UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal User principal
    ) {
        Long myUserId = Long.parseLong(principal.getUsername());
        long count = notificationService.getUnreadCount(myUserId);
        return ResponseEntity.ok(new NotificationDto.UnreadCountResponse(count));
    }

    /**
     * 알림 단건 읽음 처리
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal User principal,
            @PathVariable Long id
    ) {
        Long myUserId = Long.parseLong(principal.getUsername());
        notificationService.markAsRead(myUserId, id);
        return ResponseEntity.ok().build();
    }

    /**
     * 모든 알림 일괄 읽음 처리
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal User principal
    ) {
        Long myUserId = Long.parseLong(principal.getUsername());
        notificationService.markAllAsRead(myUserId);
        return ResponseEntity.ok().build();
    }

    /**
     * 특정 알림 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal User principal,
            @PathVariable Long id
    ) {
        Long myUserId = Long.parseLong(principal.getUsername());
        notificationService.deleteNotification(myUserId, id);
        return ResponseEntity.ok().build();
    }
}

package com.cravelog.domain.notification.dto;

import com.cravelog.domain.notification.Notification;
import com.cravelog.domain.notification.NotificationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class NotificationDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private NotificationType type;
        private String message;

        @JsonProperty("isRead")
        private boolean isRead;

        private LocalDateTime createdAt;
        private ActorInfo actor;

        public static Response from(Notification n) {
            ActorInfo actorInfo = null;
            if (n.getActor() != null) {
                actorInfo = ActorInfo.builder()
                        .id(n.getActor().getId())
                        .handle(n.getActor().getHandle())
                        .name(n.getActor().getName())
                        .profileImageUrl(n.getActor().getProfileImageUrl())
                        .build();
            }
            return Response.builder()
                    .id(n.getId())
                    .type(n.getType())
                    .message(n.getMessage())
                    .isRead(n.isRead())
                    .createdAt(n.getCreatedAt())
                    .actor(actorInfo)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActorInfo {
        private Long id;
        private String handle;
        private String name;
        private String profileImageUrl;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnreadCountResponse {
        private long unreadCount;
    }
}

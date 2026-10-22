package com.cravelog.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class FollowDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private Long id;
        private String handle;
        private String name;
        private String profileImageUrl;
        private String statusMessage;

        @JsonProperty("isFollowing")
        private boolean isFollowing;

        @JsonProperty("isPending")
        private boolean isPending;

        private Long followId;
        private LocalDateTime requestedAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FollowStatusResponse {
        @JsonProperty("isFollowing")
        private boolean isFollowing;

        @JsonProperty("isPending")
        private boolean isPending;

        private long followerCount;
        private long followingCount;
    }
}

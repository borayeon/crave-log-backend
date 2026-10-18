package com.cravelog.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
        private boolean isFollowing;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FollowStatusResponse {
        private boolean isFollowing;
        private long followerCount;
        private long followingCount;
    }
}

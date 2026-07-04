package com.cravelog.domain.user.dto;

import com.cravelog.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

public class ProfileDto {

    @Getter @Setter
    @Builder
    public static class Response {
        private String name;
        private String handle;
        private String profileImageUrl; // ⭐️ 추가
        private String role;
        private String major;
        private String location;
        private String bio;
        private String status;

        private List<String> tags;
        private List<String> goals;

        private Map<String, Object> developer;
        private Map<String, Object> career;
        private Map<String, Object> idol;

        // User 엔티티를 DTO로 변환하는 정적 팩토리 메서드
        public static Response from(User user, boolean isOwner) {
            // ⭐ NullPointerException을 막기 위해 null일 경우 빈 컬렉션으로 초기화
            Map<String, Boolean> privacy = user.getPrivacySettings();
            if (privacy == null) {
                privacy = Map.of();
            }

            Response response = Response.builder()
                    .name(user.getName())
                    .handle(user.getHandle())
                    .profileImageUrl(user.getProfileImageUrl()) // ⭐️ 추가
                    .role(user.getRole())
                    .major(user.getMajor())
                    .location(user.getLocation())
                    .bio(user.getBio())
                    .status(user.getStatusMessage())
                    .tags(user.getTags() != null ? user.getTags() : List.of())
                    .goals(user.getGoals() != null ? user.getGoals() : List.of())
                    .privacy(privacy)
                    .build();

            response.developer = (isOwner || Boolean.TRUE.equals(privacy.get("developer"))) ? user.getDeveloperData() : null;
            response.career = (isOwner || Boolean.TRUE.equals(privacy.get("career"))) ? user.getCareerData() : null;
            response.idol = (isOwner || Boolean.TRUE.equals(privacy.get("idol"))) ? user.getIdolData() : null;

            return response;
        }
    }

    @Getter @Setter
    public static class UpdateRequest {
        private String name;
        private String profileImageUrl; // ⭐️ 추가
        private String role;
        private String major;
        private String location;
        private String bio;
        private String status;

        private List<String> tags;
        private List<String> goals;

        private Map<String, Object> developer;
        private Map<String, Object> career;
        private Map<String, Object> idol;
        private Map<String, Boolean> privacy;
    }
}
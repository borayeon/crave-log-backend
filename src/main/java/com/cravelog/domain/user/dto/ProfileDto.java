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
        private String profileImageUrl;
        private String role;
        private String major;
        private String location;
        private String bio;
        private String status;

        private String oauthProvider;

        private List<String> tags;
        private List<String> goals;

        private Map<String, Object> developer;
        private Map<String, Object> career;
        private Map<String, Object> idol;

        private Map<String, Boolean> privacy;

        private List<Map<String, String>> links;

        public static Response from(User user, boolean isOwner) {
            Map<String, Boolean> privacy = user.getPrivacySettings();
            if (privacy == null) {
                privacy = Map.of();
            }

            Response response = Response.builder()
                    .name(user.getName())
                    .handle(user.getHandle())
                    .profileImageUrl(user.getProfileImageUrl())
                    .role(user.getRole())
                    .major(user.getMajor())
                    .location(user.getLocation())
                    .bio(user.getBio())
                    .status(user.getStatusMessage())
                    .oauthProvider(user.getOauthProvider())
                    .tags(user.getTags() != null ? user.getTags() : List.of())
                    .goals(user.getGoals() != null ? user.getGoals() : List.of())
                    .links(user.getLinks() != null ? user.getLinks() : List.of())
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
        private String handle;
        private String name;
        private String profileImageUrl;
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
        private List<Map<String, String>> links;
    }

    @Getter @Setter
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
    }

    @Getter @Setter
    public static class DeleteAccountRequest {
        private String password;
    }
}
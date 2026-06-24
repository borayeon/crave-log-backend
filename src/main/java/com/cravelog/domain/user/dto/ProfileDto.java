package com.cravelog.domain.user.dto;

import com.cravelog.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

public class ProfileDto {

    /**
     * 프론트엔드와 통신하기 위한 응답/요청 객체
     * React의 INITIAL_USER_DATA 구조와 1:1로 매칭되도록 설계했습니다.
     */
    @Getter @Setter
    @Builder
    public static class Response {
        private String name;
        private String handle;
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

        // User 엔티티를 DTO로 변환하는 정적 팩토리 메서드
        public static Response from(User user, boolean isOwner) {
            Response response = Response.builder()
                    .name(user.getName())
                    .handle(user.getHandle())
                    .role(user.getRole())
                    .major(user.getMajor())
                    .location(user.getLocation())
                    .bio(user.getBio())
                    .status(user.getStatusMessage())
                    .tags(user.getTags())
                    .goals(user.getGoals())
                    .privacy(user.getPrivacySettings())
                    .build();

            // ⭐ 핵심 로직: 본인이 아니면(Guest) 프라이버시 설정에 따라 데이터를 필터링합니다.
            Map<String, Boolean> privacy = user.getPrivacySettings();

            response.developer = (isOwner || Boolean.TRUE.equals(privacy.get("developer"))) ? user.getDeveloperData() : null;
            response.career = (isOwner || Boolean.TRUE.equals(privacy.get("career"))) ? user.getCareerData() : null;
            response.idol = (isOwner || Boolean.TRUE.equals(privacy.get("idol"))) ? user.getIdolData() : null;

            return response;
        }
    }

    @Getter @Setter
    public static class UpdateRequest {
        private String name;
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
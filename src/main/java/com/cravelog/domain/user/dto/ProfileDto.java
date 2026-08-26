package com.cravelog.domain.user.dto;

import com.cravelog.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashMap;
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

        // ⭐️ 기존 from 메서드 유지 (내 프로필 조회 등 파라미터가 없을 때 사용)
        public static Response from(User user, boolean isOwner) {
            return from(user, isOwner, null);
        }

        // ⭐️ 보안 필터링이 적용된 새로운 from 메서드
        public static Response from(User user, boolean isOwner, String personaId) {
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

            // 1. 기본 공개/비공개 및 소유자 확인 (⭐️ JPA Dirty Checking을 막기 위해 new HashMap으로 복사!)
            response.developer = (isOwner || Boolean.TRUE.equals(privacy.get("developer"))) ? copyMap(user.getDeveloperData()) : null;
            response.career = (isOwner || Boolean.TRUE.equals(privacy.get("career"))) ? copyMap(user.getCareerData()) : null;
            response.idol = (isOwner || Boolean.TRUE.equals(privacy.get("idol"))) ? copyMap(user.getIdolData()) : null;

            // 2. ⭐️ 백엔드 원천 차단 로직 (본인이 아니고, 특정 페르소나 파라미터가 들어왔을 때)
            if (!isOwner && personaId != null && !personaId.equals("all")) {
                List<String> allowedTabs = getAllowedTabs(response.idol, personaId);

                if (allowedTabs != null) {
                    // 최상위 탭 필터링
                    if (!allowedTabs.contains("developer")) response.developer = null;
                    if (!allowedTabs.contains("career")) response.career = null;

                    // idol Map 내부에 중첩된 탭들 필터링
                    if (response.idol != null) {
                        if (!allowedTabs.contains("businessCard")) response.idol.remove("businessCard");
                        if (!allowedTabs.contains("qna")) response.idol.remove("qna");
                        if (!allowedTabs.contains("hobby")) response.idol.remove("hobby");
                        if (!allowedTabs.contains("vision")) response.idol.remove("vision");
                        if (!allowedTabs.contains("quotes")) response.idol.remove("quotes");

                        // 프론트엔드의 memo와 art는 모두 memoArea 데이터를 사용함
                        if (!allowedTabs.contains("memo") && !allowedTabs.contains("art")) {
                            response.idol.remove("memoArea");
                        }

                        // addProfile 탭이 숨김 처리되면 관련 기본 정보들도 제거
                        if (!allowedTabs.contains("addProfile")) {
                            response.idol.remove("mbti");
                            response.idol.remove("bloodType");
                            response.idol.remove("height");
                            response.idol.remove("religion");
                            response.idol.remove("relationship");
                            response.idol.remove("languages");
                            response.idol.remove("motto");
                            response.idol.remove("recentHobby");
                            response.idol.remove("workingStyle");
                            response.idol.remove("activeHours");
                            response.idol.remove("contact");
                            response.idol.remove("tastes");
                            response.idol.remove("extraImage");
                        }
                    }
                } else {
                    // 유효하지 않은 페르소나 ID로 접근 시 모든 민감 데이터 차단
                    response.developer = null;
                    response.career = null;
                    response.idol = null;
                }
            }

            return response;
        }

        // Map 복사 헬퍼 메서드 (Entity 원본 보호용)
        private static Map<String, Object> copyMap(Map<String, Object> original) {
            return original == null ? null : new HashMap<>(original);
        }

        // 허용된 탭 추출 헬퍼 메서드
        @SuppressWarnings("unchecked")
        private static List<String> getAllowedTabs(Map<String, Object> idolData, String personaId) {
            // 1. 유저가 커스텀한 페르소나 설정이 DB(idol.personas)에 있는지 확인
            if (idolData != null && idolData.containsKey("personas")) {
                try {
                    Map<String, Object> personas = (Map<String, Object>) idolData.get("personas");
                    if (personas.containsKey(personaId)) {
                        Map<String, Object> persona = (Map<String, Object>) personas.get(personaId);
                        return (List<String>) persona.get("tabs");
                    }
                } catch (Exception e) {
                    // 파싱 에러 발생 시 디폴트 설정으로 폴백
                }
            }

            // 2. DB에 설정이 없거나 기본 제공 템플릿인 경우 디폴트 탭 반환
            switch (personaId) {
                case "portfolio": return Arrays.asList("developer", "career", "businessCard");
                case "social": return Arrays.asList("addProfile", "qna", "hobby", "art", "memo");
                case "dating": return Arrays.asList("addProfile", "vision", "qna", "hobby");
                case "fan": return Arrays.asList("hobby", "art", "memo", "quotes", "qna");
                default: return null;
            }
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
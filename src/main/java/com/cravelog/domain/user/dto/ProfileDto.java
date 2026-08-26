package com.cravelog.domain.user.dto;

import com.cravelog.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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

        public static Response from(User user, boolean isOwner) {
            return from(user, isOwner, null);
        }

        // ⭐️ 보안 필터링 완벽 적용
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

            // 1. 원본 훼손을 막기 위한 복사
            response.developer = copyMap(user.getDeveloperData());
            response.career = copyMap(user.getCareerData());
            response.idol = copyMap(user.getIdolData());

            // 2. ⭐️ 백엔드 원천 차단 로직 (본인이 아닐 때만)
            if (!isOwner) {
                List<String> allowedTabs = null;

                if (personaId != null && !personaId.equals("all")) {
                    // 특정 페르소나 접근: 해당 페르소나에 묶어둔 탭만 허용 (시크릿 링크 기능)
                    allowedTabs = getTabsFromPersona(response.idol, personaId);
                } else {
                    // 일반 방문: 개별 Privacy 설정이 '공개(true)'인 탭들만 싹 긁어서 허용
                    allowedTabs = new ArrayList<>();
                    String[] allTabKeys = {"developer", "career", "addProfile", "businessCard", "qna", "hobby", "vision", "quotes", "memo", "art"};
                    for (String key : allTabKeys) {
                        if (isPublic(privacy, key)) {
                            allowedTabs.add(key);
                        }
                    }
                }

                if (allowedTabs != null) {
                    // ⭐️ 허용되지 않은 데이터는 가차 없이 Null 처리!
                    if (!allowedTabs.contains("developer")) response.developer = null;
                    if (!allowedTabs.contains("career")) response.career = null;

                    if (response.idol != null) {
                        if (!allowedTabs.contains("businessCard")) response.idol.remove("businessCard");
                        if (!allowedTabs.contains("qna")) response.idol.remove("qna");
                        if (!allowedTabs.contains("hobby")) response.idol.remove("hobby");
                        if (!allowedTabs.contains("vision")) response.idol.remove("vision");
                        if (!allowedTabs.contains("quotes")) response.idol.remove("quotes");

                        if (!allowedTabs.contains("memo") && !allowedTabs.contains("art")) {
                            response.idol.remove("memoArea");
                        }

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
                    // 유효하지 않은 요청 시 전부 날림
                    response.developer = null;
                    response.career = null;
                    response.idol = null;
                }
            }

            return response;
        }

        private static Map<String, Object> copyMap(Map<String, Object> original) {
            return original == null ? null : new HashMap<>(original);
        }

        // ⭐️ Privacy 값이 false인지 꼼꼼히 확인하는 헬퍼 메서드
        private static boolean isPublic(Map<String, Boolean> privacy, String key) {
            if (privacy == null || !privacy.containsKey(key)) return true; // 기본은 공개
            Object val = privacy.get(key);
            if (val == null) return true;
            String str = String.valueOf(val);
            return !str.equalsIgnoreCase("false") && !str.equals("0"); // 명시적으로 비공개일 때만 false
        }

        @SuppressWarnings("unchecked")
        private static List<String> getTabsFromPersona(Map<String, Object> idolData, String personaId) {
            if (idolData != null && idolData.containsKey("personas")) {
                try {
                    Map<String, Object> personas = (Map<String, Object>) idolData.get("personas");
                    if (personas.containsKey(personaId)) {
                        Map<String, Object> persona = (Map<String, Object>) personas.get(personaId);
                        return (List<String>) persona.get("tabs");
                    }
                } catch (Exception e) {}
            }

            switch (personaId) {
                case "portfolio": return Arrays.asList("developer", "career", "businessCard");
                case "social": return Arrays.asList("addProfile", "qna", "hobby", "art", "memo");
                case "dating": return Arrays.asList("addProfile", "vision", "qna", "hobby");
                case "fan": return Arrays.asList("hobby", "art", "memo", "quotes", "qna");
                default: return null;
            }
        }
    }

    // ... UpdateRequest, ChangePasswordRequest 등 유지 ...
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
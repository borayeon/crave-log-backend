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

        // ⭐️ 난수 토큰(token) 기반 보안 검증 로직 적용
        public static Response from(User user, boolean isOwner, String token) {
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

            response.developer = copyMap(user.getDeveloperData());
            response.career = copyMap(user.getCareerData());
            response.idol = copyMap(user.getIdolData());

            if (!isOwner) {
                List<String> allowedTabs = null;

                if (token != null && !token.equals("all")) {
                    // ⭐️ 난수 토큰을 DB(personas)에서 찾아 매칭되는 탭 목록을 가져옴
                    allowedTabs = getTabsFromToken(response.idol, token);
                } else {
                    // 일반 방문: 공개(true)된 탭만 허용
                    allowedTabs = new ArrayList<>();
                    String[] allTabKeys = {"developer", "career", "addProfile", "businessCard", "qna", "hobby", "vision", "quotes", "memo", "art"};
                    for (String key : allTabKeys) {
                        if (isPublic(privacy, key)) {
                            allowedTabs.add(key);
                        }
                    }
                }

                if (allowedTabs != null) {
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
                    // 유효하지 않거나 만료된 토큰일 경우 모든 민감 데이터 즉시 차단
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

        private static boolean isPublic(Map<String, Boolean> privacy, String key) {
            if (privacy == null || !privacy.containsKey(key)) return true;
            Object val = privacy.get(key);
            if (val == null) return true;
            String str = String.valueOf(val);
            return !str.equalsIgnoreCase("false") && !str.equals("0");
        }

        // ⭐️ 토큰을 대조하여 매칭되는 페르소나의 탭을 찾아내는 검증 메서드
        @SuppressWarnings("unchecked")
        private static List<String> getTabsFromToken(Map<String, Object> idolData, String token) {
            if (idolData != null && idolData.containsKey("personas")) {
                try {
                    Map<String, Object> personas = (Map<String, Object>) idolData.get("personas");
                    for (Map.Entry<String, Object> entry : personas.entrySet()) {
                        Map<String, Object> persona = (Map<String, Object>) entry.getValue();
                        // 페르소나 객체 안에 저장된 토큰 값과 일치하는지 검사
                        String personaToken = (String) persona.get("token");
                        if (token.equals(personaToken)) {
                            return (List<String>) persona.get("tabs");
                        }
                    }
                } catch (Exception e) {}
            }

            // 구버전 호환용 (혹시 모를 구형 단어 링크 대비 폴백)
            switch (token) {
                case "portfolio": return Arrays.asList("developer", "career", "businessCard");
                case "social": return Arrays.asList("addProfile", "qna", "hobby", "art", "memo");
                case "dating": return Arrays.asList("addProfile", "vision", "qna", "hobby");
                case "fan": return Arrays.asList("hobby", "art", "memo", "quotes", "qna");
                default: return null; // ⭐️ 모르는 토큰이면 원천 차단!
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
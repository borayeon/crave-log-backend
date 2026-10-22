package com.cravelog.domain.user.dto;

import com.cravelog.domain.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
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

        private Map<String, Object> privacy;

        private List<Map<String, String>> links;

        // ⭐️ 방문자 수 카운터 (Today / Total)
        private Long todayVisits;
        private Long totalVisits;

        public static Response from(User user, boolean isOwner) {
            return from(user, isOwner, null, 0L, false, java.util.Set.of());
        }

        public static Response from(User user, boolean isOwner, String token) {
            return from(user, isOwner, token, 0L, false, java.util.Set.of());
        }

        public static Response from(User user, boolean isOwner, String token, Long todayVisits) {
            return from(user, isOwner, token, todayVisits, false, java.util.Set.of());
        }

        // ⭐️ 난수 토큰(token), 방문자수(todayVisits), 팔로워 여부(isFollower), PIN 잠금해제 목록(unlockedTargets) 서버 검증 적용
        public static Response from(User user, boolean isOwner, String token, Long todayVisits, boolean isFollower, java.util.Set<String> unlockedTargets) {
            Map<String, Object> privacy = user.getPrivacySettings();
            if (privacy == null) {
                privacy = Map.of();
            }
            java.util.Set<String> safeUnlocked = (unlockedTargets != null) ? unlockedTargets : java.util.Set.of();

            Response response = Response.builder()
                    .name(user.getName())
                    .handle(user.getHandle())
                    .profileImageUrl(user.getProfileImageUrl())
                    .role(user.getRole())
                    .major(user.getMajor())
                    .location(user.getLocation())
                    .bio(user.getBio())
                    .status(user.getStatusMessage())
                    .oauthProvider(isOwner ? user.getOauthProvider() : null)
                    .tags(user.getTags() != null ? user.getTags() : List.of())
                    .goals(user.getGoals() != null ? user.getGoals() : List.of())
                    .links(user.getLinks() != null ? user.getLinks() : List.of())
                    .privacy(privacy)
                    .todayVisits(todayVisits != null ? todayVisits : 0L)
                    .totalVisits(user.getTotalVisits())
                    .build();

            response.developer = copyMap(user.getDeveloperData());
            response.career = copyMap(user.getCareerData());
            response.idol = copyMap(user.getIdolData());

            if (!isOwner) {
                // 🛡️ 1. 게스트 응답에서 서브 탭 비밀번호(tabPasscodes) 및 페르소나 비밀번호(passcode)/시크릿 토큰 평문 유출 원천 차단
                sanitizeIdolSecrets(response.idol);

                List<String> candidateTabs = null;
                boolean isPersonaLocked = false;

                if (token != null && !token.isBlank() && !token.equals("all")) {
                    // 특정 페르소나 파라미터(?p=...) 또는 시크릿 토큰으로 접근한 경우
                    PersonaAccessResult personaAccess = resolvePersonaAccess(user.getIdolData(), token, isFollower, safeUnlocked);
                    if (!personaAccess.found) {
                        candidateTabs = null;
                    } else if (personaAccess.locked) {
                        isPersonaLocked = true;
                        candidateTabs = List.of();
                    } else {
                        candidateTabs = personaAccess.tabs;
                    }
                } else {
                    candidateTabs = Arrays.asList(
                            "developer", "developer2", "career", "addProfile",
                            "businessCard", "qna", "hobby", "vision", "quotes", "memo", "art"
                    );
                }

                if (candidateTabs != null && !isPersonaLocked) {
                    List<String> allowedTabs = new ArrayList<>();
                    for (String key : candidateTabs) {
                        if (isTabAccessible(privacy, key, isFollower, safeUnlocked)) {
                            allowedTabs.add(key);
                        }
                    }

                    filterProfileDataByAllowedTabs(response, allowedTabs);
                } else if (isPersonaLocked) {
                    // 페르소나 자체가 잠긴 경우 기본 프로필 헤더와 페르소나 메타데이터만 남기고 탭 상세 데이터는 마스킹
                    filterProfileDataByAllowedTabs(response, List.of());
                } else {
                    // 유효하지 않거나 만료된 토큰일 경우 모든 민감 데이터 즉시 차단
                    response.developer = null;
                    response.career = null;
                    response.idol = null;
                }
            }

            return response;
        }

        private static void filterProfileDataByAllowedTabs(Response response, List<String> allowedTabs) {
            boolean allowDev1 = allowedTabs.contains("developer");
            boolean allowDev2 = allowedTabs.contains("developer2");
            if (!allowDev1 && !allowDev2) {
                response.developer = null;
            } else if (response.developer != null) {
                if (!allowDev1) {
                    response.developer.remove("about");
                    response.developer.remove("techStack");
                    response.developer.remove("projects");
                    response.developer.remove("learning");
                    response.developer.remove("motto");
                }
                if (!allowDev2) {
                    response.developer.remove("experiences");
                    response.developer.remove("educations");
                    response.developer.remove("openSources");
                    response.developer.remove("presentations");
                    response.developer.remove("articles");
                }
            }

            if (!allowedTabs.contains("career")) {
                response.career = null;
            }

            if (response.idol != null) {
                if (!allowedTabs.contains("businessCard")) response.idol.remove("businessCard");
                if (!allowedTabs.contains("qna")) response.idol.remove("qna");
                if (!allowedTabs.contains("hobby")) response.idol.remove("hobby");
                if (!allowedTabs.contains("vision")) response.idol.remove("vision");
                if (!allowedTabs.contains("quotes")) response.idol.remove("quotes");

                if (!allowedTabs.contains("memo") && !allowedTabs.contains("art")) {
                    response.idol.remove("memoArea");
                } else if (response.idol.get("memoArea") instanceof Map<?, ?> rawMemo) {
                    Map<String, Object> memoCopy = new HashMap<>();
                    for (Map.Entry<?, ?> e : rawMemo.entrySet()) {
                        memoCopy.put(String.valueOf(e.getKey()), e.getValue());
                    }
                    if (!allowedTabs.contains("memo")) memoCopy.remove("text");
                    if (!allowedTabs.contains("art")) memoCopy.remove("dots");
                    response.idol.put("memoArea", memoCopy);
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
                    response.idol.remove("history");
                }
            }
        }

        // 🛡️ 게스트에게 내려가는 idol 맵에서 모든 비밀번호(tabPasscodes, persona.passcode) 제거
        @SuppressWarnings("unchecked")
        private static void sanitizeIdolSecrets(Map<String, Object> idol) {
            if (idol == null) return;
            idol.remove("tabPasscodes");

            Object personasObj = idol.get("personas");
            if (personasObj instanceof Map<?, ?> rawPersonas) {
                Map<String, Object> sanitizedPersonas = new HashMap<>();
                for (Map.Entry<?, ?> entry : rawPersonas.entrySet()) {
                    String pKey = String.valueOf(entry.getKey());
                    if (entry.getValue() instanceof Map<?, ?> pMap) {
                        Map<String, Object> pCopy = new HashMap<>();
                        for (Map.Entry<?, ?> pe : pMap.entrySet()) {
                            String fieldKey = String.valueOf(pe.getKey());
                            if (!"passcode".equals(fieldKey) && !"token".equals(fieldKey)) {
                                pCopy.put(fieldKey, pe.getValue());
                            }
                        }
                        sanitizedPersonas.put(pKey, pCopy);
                    } else {
                        sanitizedPersonas.put(pKey, entry.getValue());
                    }
                }
                idol.put("personas", sanitizedPersonas);
            }
        }

        private static Map<String, Object> copyMap(Map<String, Object> original) {
            return original == null ? null : new HashMap<>(original);
        }

        /**
         * ⭐️ 서브 탭 접근 가능 여부 판별
         * - 0단계(전체공개): true
         * - 1단계(팔로워공개): 팔로워(isFollower)인 경우만 true
         * - 2단계(PIN잠금): 서버에서 PIN 검증이 완료된 탭(unlockedTargets에 "tab:탭ID" 포함)만 true
         * - 4단계(비공개): false
         */
        private static boolean isTabAccessible(Map<String, ?> privacy, String key, boolean isFollower, java.util.Set<String> unlockedTargets) {
            int level = getTabPrivacyLevel(privacy, key);
            if (level == 0) return true;
            if (level == 1) return isFollower;
            if (level == 2) return unlockedTargets != null && unlockedTargets.contains("tab:" + key);
            return false;
        }

        public static int getTabPrivacyLevel(Map<String, ?> privacy, String key) {
            if (privacy == null || !privacy.containsKey(key)) return 0;
            Object val = privacy.get(key);
            if (val == null) return 0;
            if (val instanceof Boolean b) return b ? 0 : 4;
            String str = String.valueOf(val).trim();
            if ("true".equalsIgnoreCase(str) || "0".equals(str)) return 0;
            if ("false".equalsIgnoreCase(str) || "4".equals(str)) return 4;
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        private record PersonaAccessResult(boolean found, boolean locked, List<String> tabs) {}

        // ⭐️ 페르소나 ID 또는 토큰을 대조하여 접근 가능 여부 및 매칭 탭을 반환
        @SuppressWarnings("unchecked")
        private static PersonaAccessResult resolvePersonaAccess(
                Map<String, Object> rawIdolData,
                String tokenOrId,
                boolean isFollower,
                java.util.Set<String> unlockedTargets
        ) {
            if (rawIdolData != null && rawIdolData.get("personas") instanceof Map<?, ?> personas) {
                for (Map.Entry<?, ?> entry : personas.entrySet()) {
                    String personaKey = String.valueOf(entry.getKey());
                    if (!(entry.getValue() instanceof Map<?, ?> persona)) continue;

                    Object isActive = persona.get("isActive");
                    if (isActive != null && (Boolean.FALSE.equals(isActive) || "false".equalsIgnoreCase(String.valueOf(isActive)))) {
                        continue;
                    }

                    String personaToken = persona.get("token") != null ? String.valueOf(persona.get("token")) : null;
                    boolean matchedByToken = tokenOrId.equals(personaToken);
                    boolean matchedById = tokenOrId.equals(personaKey);

                    if (matchedByToken || matchedById) {
                        int level = 0;
                        Object visObj = persona.get("visibilityLevel");
                        if (visObj != null) {
                            try { level = Integer.parseInt(String.valueOf(visObj)); } catch (NumberFormatException ignored) {}
                        } else if (Boolean.FALSE.equals(persona.get("isVisible"))) {
                            level = 4;
                        }

                        // 1단계 팔로워 전용 페르소나인데 팔로워가 아닌 경우 잠금
                        if (level == 1 && !isFollower) {
                            return new PersonaAccessResult(true, true, List.of());
                        }
                        // 2단계 PIN 잠금 페르소나인데 아직 잠금 해제되지 않은 경우 잠금
                        if (level == 2 && (unlockedTargets == null || !unlockedTargets.contains("persona:" + personaKey))) {
                            return new PersonaAccessResult(true, true, List.of());
                        }

                        List<String> tabs = (persona.get("tabs") instanceof List<?> list)
                                ? list.stream().map(String::valueOf).toList()
                                : List.of();
                        return new PersonaAccessResult(true, false, tabs);
                    }
                }
            }

            // 구버전 호환용 폴백
            return switch (tokenOrId) {
                case "portfolio" -> new PersonaAccessResult(true, false, Arrays.asList("developer", "developer2", "career", "businessCard"));
                case "social" -> new PersonaAccessResult(true, false, Arrays.asList("addProfile", "qna", "hobby", "art", "memo"));
                case "dating" -> new PersonaAccessResult(true, false, Arrays.asList("addProfile", "vision", "qna", "hobby"));
                case "fan" -> new PersonaAccessResult(true, false, Arrays.asList("hobby", "art", "memo", "quotes", "qna"));
                default -> new PersonaAccessResult(false, true, null);
            };
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
        private Map<String, Object> privacy;
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

    /**
     * ⭐️ 사용자 검색 전용 경량 DTO (무거운 developer/career/idol JSON 제외 & 항목별 검색 공개 설정 반영)
     */
    @Getter @Setter
    @Builder
    public static class SearchResponse {
        private Long id;
        private String name;
        private String handle;
        private String profileImageUrl;
        private String role;
        private String major;
        private String location;
        private String bio;
        private String status;
        private List<String> tags;
        private Long totalVisits;
        private Long followerCount;

        @JsonProperty("isFollowing")
        private boolean isFollowing;

        @JsonProperty("isPending")
        private boolean isPending;

        private List<String> matchedFields;

        @JsonProperty("isFollowing")
        public boolean isFollowing() {
            return isFollowing;
        }

        @JsonProperty("following")
        public boolean getFollowing() {
            return isFollowing;
        }

        @JsonProperty("isPending")
        public boolean isPending() {
            return isPending;
        }

        @JsonProperty("pending")
        public boolean getPending() {
            return isPending;
        }

        public static SearchResponse from(User user, long followerCount, boolean isFollowing, List<String> matchedFields) {
            return from(user, followerCount, isFollowing, false, matchedFields);
        }

        public static SearchResponse from(User user, long followerCount, boolean isFollowing, boolean isPending, List<String> matchedFields) {
            boolean allowRole = user.isFieldSearchable("role");
            boolean allowBio = user.isFieldSearchable("bio");
            boolean allowTags = user.isFieldSearchable("tags");

            return SearchResponse.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .handle(user.getHandle())
                    .profileImageUrl(user.getProfileImageUrl())
                    .role(allowRole ? user.getRole() : null)
                    .major(allowRole ? user.getMajor() : null)
                    .location(allowRole ? user.getLocation() : null)
                    .bio(allowBio ? user.getBio() : null)
                    .status(allowBio ? user.getStatusMessage() : null)
                    .tags(allowTags && user.getTags() != null ? user.getTags() : List.of())
                    .totalVisits(user.getTotalVisits())
                    .followerCount(followerCount)
                    .isFollowing(isFollowing)
                    .isPending(isPending)
                    .matchedFields(matchedFields != null ? matchedFields : List.of())
                    .build();
        }
    }

    /**
     * ⭐️ 사용자 검색 노출 설정 변경 요청 DTO
     */
    @Getter @Setter
    public static class SearchPrivacyRequest {
        private Boolean isSearchable;
        private Boolean searchRole;
        private Boolean searchBio;
        private Boolean searchTags;
    }

    /**
     * ⭐️ 프로필 서브 탭 / 페르소나 PIN 잠금 해제 요청 DTO (서버 사이드 검증용)
     */
    @Getter @Setter
    public static class VerifyPinRequest {
        private String targetType; // "tab" 또는 "persona"
        private String targetId;   // 탭 ID (예: "career", "hobby") 또는 페르소나 키/토큰
        private String passcode;   // 입력한 PIN 번호
        private String personaId;  // 현재 조회 중인 페르소나 파라미터 (p)
        private List<String> unlockedTargets; // 이미 세션에서 잠금 해제된 목록 (예: ["tab:career", "persona:social"])
    }
}
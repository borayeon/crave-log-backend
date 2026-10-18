package com.cravelog.domain.record.dto;

import com.cravelog.domain.record.Record;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RecordDto {

    // --- 조회용 (Response) ---
    @Getter @Builder
    public static class Response {
        private String id;
        private String title;
        private String category;
        private String date;
        private String image;
        private String content;
        private String youtubeUrl;

        @JsonProperty("isPublic")
        private boolean isPublic;

        // ⭐️ 4단계 공개 레벨 (0: 전체, 1: 팔로워, 2: 비밀, 4: 비공개)
        private Integer visibilityLevel;

        // ⭐️ 잠금 상태 여부 및 잠금 유형 ("NONE", "FOLLOWER", "SECRET")
        private boolean isLocked;
        private String lockType;

        private List<String> tags;

        public static Response from(Record record) {
            return from(record, false, "NONE");
        }

        public static Response from(Record record, boolean isLocked, String lockType) {
            String displayContent = record.getContent();
            String displayImage = record.getImageUrl();
            String displayYoutube = record.getYoutubeUrl();

            // 🛡️ 접근 보안: 잠긴 경우 민감 데이터 원천 마스킹
            if (isLocked) {
                if ("FOLLOWER".equalsIgnoreCase(lockType)) {
                    displayContent = "🔒 팔로워에게만 공개된 기록입니다.";
                } else if ("SECRET".equalsIgnoreCase(lockType)) {
                    displayContent = "🔐 비밀번호가 설정된 기록입니다.";
                } else {
                    displayContent = "🔒 비공개 기록입니다.";
                }
                displayImage = null;
                displayYoutube = null;
            }

            return Response.builder()
                    .id(String.valueOf(record.getId()))
                    .title(record.getTitle())
                    .category(record.getCategoryName())
                    .date(record.getRecordDate())
                    .image(displayImage)
                    .content(displayContent)
                    .youtubeUrl(displayYoutube)
                    .isPublic(record.isPublic())
                    .visibilityLevel(record.getVisibilityLevel() != null ? record.getVisibilityLevel() : (record.isPublic() ? 0 : 4))
                    .isLocked(isLocked)
                    .lockType(lockType)
                    .tags(isLocked ? Collections.emptyList() : record.getRecordTags().stream()
                            .map(rt -> rt.getTag().getName())
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    // --- 생성용 (Request) ---
    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateRequest {
        private String title;
        private String categoryName;
        private String recordDate;
        private String imageUrl;
        private String content;
        private String youtubeUrl;

        @JsonProperty("isPublic")
        private boolean isPublic = true;

        private Integer visibilityLevel = 0;
        private String passcode;

        private List<Long> tagIds;
        private List<String> newTags;
    }

    // --- 수정용 (Request) ---
    @Getter
    @Setter
    @NoArgsConstructor
    public static class UpdateRequest {
        private String title;
        private String categoryName;
        private String recordDate;
        private String imageUrl;
        private String content;
        private String youtubeUrl;

        @JsonProperty("isPublic")
        private boolean isPublic;

        private Integer visibilityLevel;
        private String passcode;

        private List<Long> tagIds;
    }

    // --- 공개 레벨 단독 변경용 (Request) ---
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateVisibilityRequest {
        private Integer visibilityLevel;
        private String passcode;
    }

    // --- 2단계 비밀번호 검증용 (Request) ---
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyPasscodeRequest {
        private String passcode;
    }
}
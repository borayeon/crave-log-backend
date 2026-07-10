package com.cravelog.domain.record.dto;

import com.cravelog.domain.record.Record;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

        @JsonProperty("isPublic") // ⭐️ JSON 매핑 시 'is'가 사라지는 문제 해결!
        private boolean isPublic;

        private List<String> tags;

        public static Response from(Record record) {
            return Response.builder()
                    .id(String.valueOf(record.getId()))
                    .title(record.getTitle())
                    .category(record.getCategoryName())
                    .date(record.getRecordDate())
                    .image(record.getImageUrl())
                    .content(record.getContent())
                    .youtubeUrl(record.getYoutubeUrl())
                    .isPublic(record.isPublic())
                    .tags(record.getRecordTags().stream()
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
        private boolean isPublic;

        private List<Long> tagIds;
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

        private List<Long> tagIds;
    }
}
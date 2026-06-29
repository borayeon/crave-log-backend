package com.cravelog.domain.record.dto;

import com.cravelog.domain.record.Record;
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
        private List<String> tags;

        public static Response from(Record record) {
            return Response.builder()
                    .id(String.valueOf(record.getId()))
                    .title(record.getTitle())
                    .category(record.getCategoryName())
                    .date(record.getRecordDate())
                    .image(record.getImageUrl())
                    .tags(record.getRecordTags().stream()
                            .map(rt -> rt.getTag().getName())
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    // --- 생성용 (Request) ---
    @Getter @Setter
    @NoArgsConstructor
    public static class CreateRequest {
        private String title;
        private String categoryName;
        private String recordDate;
        private String imageUrl;
        private boolean isPublic;
        private List<Long> tagIds; // 선택한 태그들의 고유 ID 목록
    }
}
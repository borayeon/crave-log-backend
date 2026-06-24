package com.cravelog.domain.record.dto;

import com.cravelog.domain.record.Record;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

public class RecordDto {

    @Getter @Builder
    public static class Response {
        private String id;
        private String title;
        private String category;
        private String date;
        private String image;
        private List<String> tags; // 태그 이름 목록

        public static Response from(Record record) {
            return Response.builder()
                    .id(String.valueOf(record.getId()))
                    .title(record.getTitle())
                    .category(record.getCategoryName())
                    .date(record.getRecordDate())
                    .image(record.getImageUrl())
                    // RecordTag 중간 테이블을 거쳐 Tag의 이름만 추출
                    .tags(record.getRecordTags().stream()
                            .map(rt -> rt.getTag().getName())
                            .collect(Collectors.toList()))
                    .build();
        }
    }
}
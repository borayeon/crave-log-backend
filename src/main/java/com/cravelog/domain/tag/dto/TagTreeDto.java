package com.cravelog.domain.tag.dto;

import com.cravelog.domain.tag.Category;
import com.cravelog.domain.tag.Tag;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

public class TagTreeDto {

    // --- 조회용 (Response) ---
    @Getter @Builder
    public static class CategoryResponse {
        private String id;
        private String name;
        private List<TagResponse> children;

        public static CategoryResponse from(Category category) {
            return CategoryResponse.builder()
                    .id(String.valueOf(category.getId())) // 프론트와 맞추기 위해 String 변환
                    .name(category.getName())
                    .children(category.getTags().stream()
                            .map(TagResponse::from)
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    @Getter @Builder
    public static class TagResponse {
        private String id;
        private String name;

        public static TagResponse from(Tag tag) {
            return TagResponse.builder()
                    .id(String.valueOf(tag.getId()))
                    .name(tag.getName())
                    .build();
        }
    }

    // --- 생성용 (Request) ---
    @Getter @Setter
    @NoArgsConstructor
    public static class CategoryCreateRequest {
        private String name;
    }

    @Getter @Setter
    @NoArgsConstructor
    public static class TagCreateRequest {
        private String name;
    }
}
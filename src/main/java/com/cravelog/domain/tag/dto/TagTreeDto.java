package com.cravelog.domain.tag.dto;

import com.cravelog.domain.tag.Category;
import com.cravelog.domain.tag.Tag;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

public class TagTreeDto {

    @Getter @Builder
    public static class CategoryResponse {
        private String id;
        private String name;
        private List<TagResponse> children;

        public static CategoryResponse from(Category category) {
            return CategoryResponse.builder()
                    .id("cat_" + category.getId()) // 프론트엔드의 문자열 id와 맞춤
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
                    .id("tag_" + tag.getId())
                    .name(tag.getName())
                    .build();
        }
    }
}
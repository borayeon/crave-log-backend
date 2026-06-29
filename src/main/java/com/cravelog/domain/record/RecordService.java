package com.cravelog.domain.record;

import com.cravelog.domain.record.dto.RecordDto;
import com.cravelog.domain.tag.Category;
import com.cravelog.domain.tag.CategoryRepository;
import com.cravelog.domain.tag.Tag;
import com.cravelog.domain.tag.TagRepository; // 추가 필요
import com.cravelog.domain.tag.dto.TagTreeDto;
import com.cravelog.domain.user.User;
import com.cravelog.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordRepository recordRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    // --- 조회 (READ) ---
    @Transactional(readOnly = true)
    public List<TagTreeDto.CategoryResponse> getTagTree(String handle) {
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        return categoryRepository.findAllByUserId(user.getId()).stream()
                .map(TagTreeDto.CategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RecordDto.Response> getRecords(String handle, boolean isOwner) {
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        List<Record> records = recordRepository.findAllByUserIdWithTags(user.getId());

        return records.stream()
                .filter(record -> isOwner || record.isPublic())
                .map(RecordDto.Response::from)
                .collect(Collectors.toList());
    }

    // --- 카테고리/태그 생성 및 삭제 (C, D) ---
    @Transactional
    public void createCategory(Long userId, TagTreeDto.CategoryCreateRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        Category category = new Category(user, request.getName());
        categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        // 본인의 카테고리인지 검증 로직이 들어가면 좋습니다.
        categoryRepository.deleteById(categoryId);
    }

    @Transactional
    public void createTag(Long userId, Long categoryId, TagTreeDto.TagCreateRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리가 없습니다."));

        Tag tag = new Tag(user, category, request.getName());
        tagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(Long userId, Long tagId) {
        tagRepository.deleteById(tagId);
    }

    // --- 기록 생성 및 삭제 (C, D) ---
    @Transactional
    public void createRecord(Long userId, RecordDto.CreateRequest request) {
        User user = userRepository.findById(userId).orElseThrow();

        // 1. Record 엔티티 생성
        Record record = Record.builder()
                .user(user)
                .title(request.getTitle())
                .categoryName(request.getCategoryName())
                .recordDate(request.getRecordDate())
                .imageUrl(request.getImageUrl())
                .isPublic(request.isPublic())
                .build();

        // 2. 전달받은 태그 ID 목록으로 Tag를 찾아서 RecordTag로 묶기
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(request.getTagIds());
            for (Tag tag : tags) {
                RecordTag recordTag = new RecordTag(record, tag);
                record.getRecordTags().add(recordTag);
            }
        }

        recordRepository.save(record);
    }

    @Transactional
    public void deleteRecord(Long userId, Long recordId) {
        recordRepository.deleteById(recordId);
    }
}
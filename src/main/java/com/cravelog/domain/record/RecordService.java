package com.cravelog.domain.record;

import com.cravelog.domain.record.dto.RecordDto;
import com.cravelog.domain.tag.Category;
import com.cravelog.domain.tag.CategoryRepository;
import com.cravelog.domain.tag.Tag;
import com.cravelog.domain.tag.TagRepository;
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

    // --- 트리(카테고리/태그) 관련 로직 ---

    @Transactional(readOnly = true)
    public List<TagTreeDto.CategoryResponse> getTagTree(String handle) {
        User user = userRepository.findByHandle(handle).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return categoryRepository.findAllByUserId(user.getId()).stream()
                .map(TagTreeDto.CategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TagTreeDto.CategoryResponse> getMyTagTree(Long userId) {
        return categoryRepository.findAllByUserId(userId).stream()
                .map(TagTreeDto.CategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createCategory(Long userId, TagTreeDto.CategoryCreateRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        Category category = new Category(user, request.getName());
        categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow();
        if (!category.getUser().getId().equals(userId)) throw new IllegalArgumentException("권한이 없습니다.");
        categoryRepository.delete(category);
    }

    @Transactional
    public void createTag(Long userId, Long categoryId, TagTreeDto.TagCreateRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        Category category = categoryRepository.findById(categoryId).orElseThrow();
        if (!category.getUser().getId().equals(userId)) throw new IllegalArgumentException("권한이 없습니다.");
        Tag tag = new Tag(user, category, request.getName());
        tagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(Long userId, Long tagId) {
        Tag tag = tagRepository.findById(tagId).orElseThrow();
        if (!tag.getUser().getId().equals(userId)) throw new IllegalArgumentException("권한이 없습니다.");
        tagRepository.delete(tag);
    }


    // --- 기록(Record) 관련 로직 ---

    /**
     * ⭐️ 특정 유저의 퍼블릭 기록 목록 조회 (누락되었던 코드 복구!)
     */
    @Transactional(readOnly = true)
    public List<RecordDto.Response> getPublicRecords(String handle) {
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return recordRepository.findAllByUserIdWithTags(user.getId()).stream()
                .filter(Record::isPublic) // 공개(true)된 기록만 필터링해서 내보냅니다!
                .map(RecordDto.Response::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RecordDto.Response> getMyRecords(Long userId) {
        return recordRepository.findAllByUserIdWithTags(userId).stream()
                .map(RecordDto.Response::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createRecord(Long userId, RecordDto.CreateRequest request) {
        User user = userRepository.findById(userId).orElseThrow();

        Record record = Record.builder()
                .user(user)
                .title(request.getTitle())
                .categoryName(request.getCategoryName())
                .recordDate(request.getRecordDate())
                .imageUrl(request.getImageUrl())
                .content(request.getContent())
                .isPublic(request.isPublic())
                .build();

        // 태그 매핑
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(request.getTagIds());
            tags.forEach(tag -> {
                RecordTag recordTag = new RecordTag(record, tag);
                record.getRecordTags().add(recordTag);
            });
        }

        recordRepository.save(record);
    }

    @Transactional
    public void updateRecord(Long userId, Long recordId, RecordDto.UpdateRequest request) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("기록을 찾을 수 없습니다."));

        if (!record.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        record.update(request.getTitle(), request.getCategoryName(), request.getRecordDate(), request.getImageUrl(), request.getContent(), request.isPublic());

        record.getRecordTags().clear();

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(request.getTagIds());
            tags.forEach(tag -> {
                RecordTag recordTag = new RecordTag(record, tag);
                record.getRecordTags().add(recordTag);
            });
        }
    }

    @Transactional
    public void deleteRecord(Long userId, Long recordId) {
        Record record = recordRepository.findById(recordId).orElseThrow();
        if (!record.getUser().getId().equals(userId)) throw new IllegalArgumentException("권한이 없습니다.");
        recordRepository.delete(record);
    }
}
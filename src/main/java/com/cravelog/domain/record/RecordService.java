package com.cravelog.domain.record;

import com.cravelog.domain.record.dto.RecordDto;
import com.cravelog.domain.tag.Category;
import com.cravelog.domain.tag.CategoryRepository;
import com.cravelog.domain.tag.Tag;
import com.cravelog.domain.tag.TagRepository;
import com.cravelog.domain.tag.dto.TagTreeDto;
import com.cravelog.domain.user.FollowRepository;
import com.cravelog.domain.user.User;
import com.cravelog.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordRepository recordRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    // --- 트리(카테고리/태그) 관련 로직 ---

    @Transactional(readOnly = true)
    public List<TagTreeDto.CategoryResponse> getTagTree(String handle) {
        User user = userRepository.findByHandle(handle).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getUserRole() == com.cravelog.domain.user.UserRole.ROLE_ADMIN) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
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
     * ⭐️ 특정 유저의 기록 목록 조회 (4단계 접근 보안 필터링 적용)
     * - 본인: 모든 기록(비공개 포함) 정상 조회
     * - 타인/게스트:
     *   - 0단계 (전체공개): 전체 노출
     *   - 1단계 (팔로워공개): 팔로워가 아니면 본문/미디어 마스킹 및 FOLLOWER 잠금 카드
     *   - 2단계 (비밀공개): SECRET 잠금 카드 (비밀번호 검증 필요)
     *   - 4단계 (비공개): 목록에서 원천 제외
     */
    @Transactional(readOnly = true)
    public List<RecordDto.Response> getPublicRecords(String handle, Long currentUserId) {
        User targetUser = userRepository.findByHandle(handle)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (targetUser.getUserRole() == com.cravelog.domain.user.UserRole.ROLE_ADMIN) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        List<Record> records = recordRepository.findAllByUserIdWithTags(targetUser.getId());
        boolean isOwner = currentUserId != null && currentUserId.equals(targetUser.getId());

        if (isOwner) {
            return records.stream()
                    .map(RecordDto.Response::from)
                    .collect(Collectors.toList());
        }

        boolean isFollower = currentUserId != null && followRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUser.getId());

        List<RecordDto.Response> responses = new ArrayList<>();
        for (Record r : records) {
            int level = r.getVisibilityLevel() != null ? r.getVisibilityLevel() : (r.isPublic() ? 0 : 4);

            if (level == 4) {
                // 비공개: 타인에게는 절대 노출하지 않음
                continue;
            } else if (level == 1) {
                // 팔로워 공개
                if (isFollower) {
                    responses.add(RecordDto.Response.from(r, false, "NONE"));
                } else {
                    responses.add(RecordDto.Response.from(r, true, "FOLLOWER"));
                }
            } else if (level == 2) {
                // 비밀 공개: 기본적으로 잠금 상태로 반환 (비밀번호 인증 필요)
                responses.add(RecordDto.Response.from(r, true, "SECRET"));
            } else {
                // 0단계: 전체 공개
                responses.add(RecordDto.Response.from(r, false, "NONE"));
            }
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public List<RecordDto.Response> getMyRecords(Long userId) {
        return recordRepository.findAllByUserIdWithTags(userId).stream()
                .map(RecordDto.Response::from)
                .collect(Collectors.toList());
    }

    /**
     * ⭐️ 2단계 비밀 공개 기록의 비밀번호(패스코드) 검증 및 원본 데이터 반환
     */
    @Transactional(readOnly = true)
    public RecordDto.Response verifyPasscode(Long recordId, String passcode) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("기록을 찾을 수 없습니다."));

        int level = record.getVisibilityLevel() != null ? record.getVisibilityLevel() : 0;
        if (level == 2) {
            if (record.getPasscode() != null && !record.getPasscode().equals(passcode)) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
        }

        return RecordDto.Response.from(record, false, "NONE");
    }

    /**
     * ⭐️ 기록의 공개 레벨(0, 1, 2, 4) 및 비밀번호 변경
     */
    @Transactional
    public void updateVisibility(Long userId, Long recordId, Integer visibilityLevel, String passcode) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("기록을 찾을 수 없습니다."));

        if (!record.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        record.updateVisibility(visibilityLevel, passcode);
    }

    @Transactional
    public void createRecord(Long userId, RecordDto.CreateRequest request) {
        User user = userRepository.findById(userId).orElseThrow();

        Category category = categoryRepository.findByNameAndUserId(request.getCategoryName(), userId)
                .orElseGet(() -> {
                    Category newCat = new Category(user, request.getCategoryName());
                    return categoryRepository.save(newCat);
                });

        Record record = Record.builder()
                .user(user)
                .title(request.getTitle())
                .categoryName(request.getCategoryName())
                .recordDate(request.getRecordDate())
                .imageUrl(request.getImageUrl())
                .youtubeUrl(request.getYoutubeUrl())
                .content(request.getContent())
                .isPublic(request.isPublic())
                .visibilityLevel(request.getVisibilityLevel())
                .passcode(request.getPasscode())
                .build();

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(request.getTagIds());
            tags.forEach(tag -> {
                RecordTag recordTag = new RecordTag(record, tag);
                record.getRecordTags().add(recordTag);
            });
        }

        if (request.getNewTags() != null && !request.getNewTags().isEmpty()) {
            for (String newTagName : request.getNewTags()) {
                Tag tag = tagRepository.findByNameAndCategoryId(newTagName, category.getId())
                        .orElseGet(() -> {
                            Tag newTag = new Tag(user, category, newTagName);
                            return tagRepository.save(newTag);
                        });

                RecordTag recordTag = new RecordTag(record, tag);
                record.getRecordTags().add(recordTag);
            }
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

        record.update(
                request.getTitle(),
                request.getCategoryName(),
                request.getRecordDate(),
                request.getImageUrl(),
                request.getYoutubeUrl(),
                request.getContent(),
                request.isPublic(),
                request.getVisibilityLevel(),
                request.getPasscode()
        );
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
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("기록을 찾을 수 없습니다."));

        if (!record.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        recordRepository.delete(record);
    }
}
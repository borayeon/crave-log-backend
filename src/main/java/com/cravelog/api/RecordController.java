package com.cravelog.api;

import com.cravelog.domain.record.RecordService;
import com.cravelog.domain.record.dto.RecordDto;
import com.cravelog.domain.tag.dto.TagTreeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    /**
     * 1. 특정 유저의 퍼블릭 카테고리/태그 트리 조회 (게스트용)
     */
    @GetMapping("/users/{handle}/categories")
    public ResponseEntity<List<TagTreeDto.CategoryResponse>> getTagTree(@PathVariable String handle) {
        List<TagTreeDto.CategoryResponse> response = recordService.getTagTree(handle);
        return ResponseEntity.ok(response);
    }

    /**
     * 1-1. 내 카테고리/태그 트리 조회 (마이페이지용)
     */
    @GetMapping("/me/categories")
    public ResponseEntity<List<TagTreeDto.CategoryResponse>> getMyTagTree(@AuthenticationPrincipal User principal) {
        Long myUserId = Long.parseLong(principal.getUsername());
        List<TagTreeDto.CategoryResponse> response = recordService.getMyTagTree(myUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * ⭐️ 2. 특정 유저의 기록 목록 조회 (4단계 접근 보안 필터링 적용)
     */
    @GetMapping("/users/{handle}/records")
    public ResponseEntity<List<RecordDto.Response>> getPublicRecords(
            @AuthenticationPrincipal User principal,
            @PathVariable String handle) {
        Long currentUserId = principal != null ? Long.parseLong(principal.getUsername()) : null;
        List<RecordDto.Response> response = recordService.getPublicRecords(handle, currentUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * 3. 내 기록 전체 목록 조회 (마이페이지용 - 비공개 포함)
     */
    @GetMapping("/me/records")
    public ResponseEntity<List<RecordDto.Response>> getMyRecords(@AuthenticationPrincipal User principal) {
        Long myUserId = Long.parseLong(principal.getUsername());
        List<RecordDto.Response> response = recordService.getMyRecords(myUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * 4. 새로운 기록 추가
     */
    @PostMapping("/me/records")
    public ResponseEntity<Void> createRecord(@AuthenticationPrincipal User principal, @RequestBody RecordDto.CreateRequest request) {
        Long myUserId = Long.parseLong(principal.getUsername());
        recordService.createRecord(myUserId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 4-1. 기존 기록 수정
     */
    @PutMapping("/me/records/{recordId}")
    public ResponseEntity<Void> updateRecord(@AuthenticationPrincipal User principal, @PathVariable Long recordId, @RequestBody RecordDto.UpdateRequest request) {
        Long myUserId = Long.parseLong(principal.getUsername());
        recordService.updateRecord(myUserId, recordId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * ⭐️ 4-2. 기록 공개 권한 단독 변경 (0단계 전체, 1단계 팔로워, 2단계 비밀, 4단계 비공개)
     */
    @PatchMapping("/me/records/{recordId}/visibility")
    public ResponseEntity<Void> updateVisibility(
            @AuthenticationPrincipal User principal,
            @PathVariable Long recordId,
            @RequestBody RecordDto.UpdateVisibilityRequest request) {
        Long myUserId = Long.parseLong(principal.getUsername());
        recordService.updateVisibility(myUserId, recordId, request.getVisibilityLevel(), request.getPasscode());
        return ResponseEntity.ok().build();
    }

    /**
     * ⭐️ 4-3. 2단계 비밀 공개 기록의 패스코드 검증 및 잠금 해제
     */
    @PostMapping("/records/{recordId}/verify-passcode")
    public ResponseEntity<RecordDto.Response> verifyPasscode(
            @PathVariable Long recordId,
            @RequestBody RecordDto.VerifyPasscodeRequest request) {
        RecordDto.Response response = recordService.verifyPasscode(recordId, request.getPasscode());
        return ResponseEntity.ok(response);
    }

    /**
     * 5. 특정 기록 삭제
     */
    @DeleteMapping("/me/records/{recordId}")
    public ResponseEntity<Void> deleteRecord(@AuthenticationPrincipal User principal, @PathVariable Long recordId) {
        Long myUserId = Long.parseLong(principal.getUsername());
        recordService.deleteRecord(myUserId, recordId);
        return ResponseEntity.ok().build();
    }

    /**
     * 6. 새 카테고리(폴더) 추가
     */
    @PostMapping("/me/categories")
    public ResponseEntity<Void> createCategory(@AuthenticationPrincipal User principal, @RequestBody TagTreeDto.CategoryCreateRequest request) {
        Long myUserId = Long.parseLong(principal.getUsername());
        recordService.createCategory(myUserId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 7. 특정 카테고리(폴더) 삭제
     */
    @DeleteMapping("/me/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@AuthenticationPrincipal User principal, @PathVariable Long categoryId) {
        Long myUserId = Long.parseLong(principal.getUsername());
        recordService.deleteCategory(myUserId, categoryId);
        return ResponseEntity.ok().build();
    }

    /**
     * 8. 특정 카테고리에 새 태그 추가
     */
    @PostMapping("/me/categories/{categoryId}/tags")
    public ResponseEntity<Void> createTag(@AuthenticationPrincipal User principal, @PathVariable Long categoryId, @RequestBody TagTreeDto.TagCreateRequest request) {
        Long myUserId = Long.parseLong(principal.getUsername());
        recordService.createTag(myUserId, categoryId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 9. 특정 태그 삭제
     */
    @DeleteMapping("/me/tags/{tagId}")
    public ResponseEntity<Void> deleteTag(@AuthenticationPrincipal User principal, @PathVariable Long tagId) {
        Long myUserId = Long.parseLong(principal.getUsername());
        recordService.deleteTag(myUserId, tagId);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
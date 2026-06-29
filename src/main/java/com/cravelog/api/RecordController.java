package com.cravelog.api;

import com.cravelog.domain.record.RecordService;
import com.cravelog.domain.record.dto.RecordDto;
import com.cravelog.domain.tag.dto.TagTreeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    /**
     * 1. 특정 유저의 카테고리/태그 트리 조회 (퍼블릭)
     */
    @GetMapping("/users/{handle}/categories")
    public ResponseEntity<List<TagTreeDto.CategoryResponse>> getTagTree(@PathVariable String handle) {
        List<TagTreeDto.CategoryResponse> response = recordService.getTagTree(handle);
        return ResponseEntity.ok(response);
    }

    /**
     * 2. 특정 유저의 퍼블릭 기록 목록 조회 (게스트용 - 비공개 필터링됨)
     */
    @GetMapping("/users/{handle}/records")
    public ResponseEntity<List<RecordDto.Response>> getPublicRecords(@PathVariable String handle) {
        List<RecordDto.Response> response = recordService.getRecords(handle, false);
        return ResponseEntity.ok(response);
    }

    /**
     * 3. 내 기록 전체 목록 조회 (마이페이지용 - 비공개 포함)
     */
    @GetMapping("/me/records")
    public ResponseEntity<List<RecordDto.Response>> getMyRecords() {
        String myHandle = "taekyeong.dev"; // TODO: 시큐리티 적용 후 현재 로그인 유저 정보로 교체
        List<RecordDto.Response> response = recordService.getRecords(myHandle, true);
        return ResponseEntity.ok(response);
    }

    /**
     * 4. 새로운 기록 추가
     */
    @PostMapping("/me/records")
    public ResponseEntity<Void> createRecord(@RequestBody RecordDto.CreateRequest request) {
        Long myUserId = 1L; // TODO: 시큐리티 적용 후 현재 로그인 유저 ID로 교체
        recordService.createRecord(myUserId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 5. 특정 기록 삭제
     */
    @DeleteMapping("/me/records/{recordId}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long recordId) {
        Long myUserId = 1L; // TODO: 시큐리티 적용 후 현재 로그인 유저 ID로 교체
        recordService.deleteRecord(myUserId, recordId);
        return ResponseEntity.ok().build();
    }

    /**
     * 6. 새 카테고리(폴더) 추가
     */
    @PostMapping("/me/categories")
    public ResponseEntity<Void> createCategory(@RequestBody TagTreeDto.CategoryCreateRequest request) {
        Long myUserId = 1L; // TODO: 시큐리티 적용 후 현재 로그인 유저 ID로 교체
        recordService.createCategory(myUserId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 7. 특정 카테고리(폴더) 삭제
     */
    @DeleteMapping("/me/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        Long myUserId = 1L; // TODO: 시큐리티 적용 후 현재 로그인 유저 ID로 교체
        recordService.deleteCategory(myUserId, categoryId);
        return ResponseEntity.ok().build();
    }

    /**
     * 8. 특정 카테고리에 새 태그 추가
     */
    @PostMapping("/me/categories/{categoryId}/tags")
    public ResponseEntity<Void> createTag(@PathVariable Long categoryId, @RequestBody TagTreeDto.TagCreateRequest request) {
        Long myUserId = 1L; // TODO: 시큐리티 적용 후 현재 로그인 유저 ID로 교체
        recordService.createTag(myUserId, categoryId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 9. 특정 태그 삭제
     */
    @DeleteMapping("/me/tags/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long tagId) {
        Long myUserId = 1L; // TODO: 시큐리티 적용 후 현재 로그인 유저 ID로 교체
        recordService.deleteTag(myUserId, tagId);
        return ResponseEntity.ok().build();
    }
}
package com.cravelog.api.controller;

import com.cravelog.api.domain.archive.ArchiveBlock;
import com.cravelog.api.dto.ArchiveBlockRequest;
import com.cravelog.api.service.ArchiveBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/archives")
@RequiredArgsConstructor
public class ArchiveBlockController {

    private final ArchiveBlockService archiveBlockService;

    /**
     * [조회] 특정 유저의 아카이브 그리드 데이터 조회
     * (추후 @AuthenticationPrincipal 을 통해 로그인한 visitorId 연동 권장)
     */
    @GetMapping("/users/{ownerId}")
    public ResponseEntity<List<ArchiveBlock>> getArchives(
            @PathVariable Long ownerId,
            @RequestParam(required = false) Long visitorId
    ) {
        List<ArchiveBlock> blocks = archiveBlockService.getPermittedArchives(visitorId, ownerId);
        return ResponseEntity.ok(blocks);
    }

    /**
     * [생성] 내 공간에 새로운 아카이브 블록 추가
     */
    @PostMapping
    public ResponseEntity<ArchiveBlock> createBlock(
            @RequestParam Long userId, // 인증 토큰 파싱 전 임시 파라미터
            @RequestBody ArchiveBlockRequest request
    ) {
        ArchiveBlock createdBlock = archiveBlockService.createBlock(userId, request);
        return ResponseEntity.ok(createdBlock);
    }

    /**
     * [수정] 아카이브 블록 정보 수정 (제목, 설명, 공간 이동 등)
     */
    @PutMapping("/{blockId}")
    public ResponseEntity<ArchiveBlock> updateBlock(
            @PathVariable Long blockId,
            @RequestParam Long userId, // 인증 토큰 파싱 전 임시 파라미터
            @RequestBody ArchiveBlockRequest request
    ) {
        ArchiveBlock updatedBlock = archiveBlockService.updateBlock(userId, blockId, request);
        return ResponseEntity.ok(updatedBlock);
    }

    /**
     * [삭제] 아카이브 블록 삭제
     */
    @DeleteMapping("/{blockId}")
    public ResponseEntity<Void> deleteBlock(
            @PathVariable Long blockId,
            @RequestParam Long userId // 인증 토큰 파싱 전 임시 파라미터
    ) {
        archiveBlockService.deleteBlock(userId, blockId);
        return ResponseEntity.ok().build();
    }
}
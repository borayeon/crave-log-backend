package com.cravelog.api.controller;

import com.cravelog.api.domain.archive.ArchiveBlock;
import com.cravelog.api.dto.ArchiveBlockRequest;
import com.cravelog.api.service.ArchiveBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/archives")
@RequiredArgsConstructor
public class ArchiveBlockController {

    private final ArchiveBlockService archiveBlockService;

    /**
     * [조회] 특정 유저(ownerId)의 아카이브 그리드 데이터 조회
     * visitorId는 토큰을 해석하여 알아냅니다. (로그인 안 한 사람은 null)
     */
    @GetMapping("/users/{ownerId}")
    public ResponseEntity<List<ArchiveBlock>> getArchives(
            @PathVariable Long ownerId,
            @AuthenticationPrincipal Long visitorId // 이제 안전하게 토큰에서 유저 ID를 빼옵니다!
    ) {
        List<ArchiveBlock> blocks = archiveBlockService.getPermittedArchives(visitorId, ownerId);
        return ResponseEntity.ok(blocks);
    }

    /**
     * [생성] 내 공간에 새로운 아카이브 블록 추가
     */
    @PostMapping
    public ResponseEntity<ArchiveBlock> createBlock(
            @AuthenticationPrincipal Long userId, // 내 토큰 기반 아이디
            @RequestBody ArchiveBlockRequest request
    ) {
        ArchiveBlock createdBlock = archiveBlockService.createBlock(userId, request);
        return ResponseEntity.ok(createdBlock);
    }

    /**
     * [수정] 아카이브 블록 정보 수정
     */
    @PutMapping("/{blockId}")
    public ResponseEntity<ArchiveBlock> updateBlock(
            @PathVariable Long blockId,
            @AuthenticationPrincipal Long userId,
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
            @AuthenticationPrincipal Long userId
    ) {
        archiveBlockService.deleteBlock(userId, blockId);
        return ResponseEntity.ok().build();
    }
}
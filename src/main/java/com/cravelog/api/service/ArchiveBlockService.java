package com.cravelog.api.service;

import com.cravelog.api.domain.archive.ArchiveBlock;
import com.cravelog.api.domain.archive.ArchiveBlockRepository;
import com.cravelog.api.domain.enums.SpaceType;
import com.cravelog.api.domain.relationship.Relationship;
import com.cravelog.api.domain.relationship.RelationshipRepository;
import com.cravelog.api.domain.user.User;
import com.cravelog.api.domain.user.UserRepository;
import com.cravelog.api.dto.ArchiveBlockRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArchiveBlockService {

    private final ArchiveBlockRepository archiveBlockRepository;
    private final RelationshipRepository relationshipRepository;
    private final UserRepository userRepository;

    /**
     * [조회] 방문자(visitorId)의 권한을 파악하여, 주인(ownerId)의 허용된 기록만 반환
     */
    public List<ArchiveBlock> getPermittedArchives(Long visitorId, Long ownerId) {
        SpaceType grantedSpace = SpaceType.OPEN;

        if (visitorId != null) {
            if (visitorId.equals(ownerId)) {
                grantedSpace = SpaceType.MINE; // 자기 자신
            } else {
                grantedSpace = relationshipRepository.findByFollowerIdAndFollowingId(visitorId, ownerId)
                        .map(Relationship::getGrantedSpace)
                        .orElse(SpaceType.OPEN);
            }
        }

        final SpaceType finalGrantedSpace = grantedSpace;

        return archiveBlockRepository.findByUserIdOrderByCreatedAtDesc(ownerId)
                .stream()
                .filter(block -> block.getSpaceType().ordinal() <= finalGrantedSpace.ordinal())
                .collect(Collectors.toList());
    }

    /**
     * [생성] 새로운 아카이브 기록 생성
     */
    @Transactional
    public ArchiveBlock createBlock(Long userId, ArchiveBlockRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ArchiveBlock block = ArchiveBlock.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .url(request.getUrl())
                .type(request.getType())
                .spaceType(request.getSpaceType())
                .build();

        return archiveBlockRepository.save(block);
    }

    /**
     * [수정] 기존 아카이브 기록 수정 (본인 소유인지 검증)
     */
    @Transactional
    public ArchiveBlock updateBlock(Long userId, Long blockId, ArchiveBlockRequest request) {
        ArchiveBlock block = archiveBlockRepository.findById(blockId)
                .orElseThrow(() -> new IllegalArgumentException("기록을 찾을 수 없습니다."));

        if (!block.getUser().getId().equals(userId)) {
            throw new IllegalStateException("이 기록을 수정할 권한이 없습니다.");
        }

        block.update(request.getTitle(), request.getContent(), request.getUrl(), request.getType(), request.getSpaceType());
        return block; // Dirty Checking에 의해 자동 업데이트 됨
    }

    /**
     * [삭제] 기존 아카이브 기록 삭제 (본인 소유인지 검증)
     */
    @Transactional
    public void deleteBlock(Long userId, Long blockId) {
        ArchiveBlock block = archiveBlockRepository.findById(blockId)
                .orElseThrow(() -> new IllegalArgumentException("기록을 찾을 수 없습니다."));

        if (!block.getUser().getId().equals(userId)) {
            throw new IllegalStateException("이 기록을 삭제할 권한이 없습니다.");
        }

        archiveBlockRepository.delete(block);
    }
}

package com.cravelog.api.domain.archive;

import com.cravelog.api.domain.enums.SpaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArchiveBlockRepository extends JpaRepository<ArchiveBlock, Long> {
    // 특정 유저의 특정 공간 기록 조회
    List<ArchiveBlock> findByUserIdAndSpaceTypeOrderByCreatedAtDesc(Long userId, SpaceType spaceType);
}
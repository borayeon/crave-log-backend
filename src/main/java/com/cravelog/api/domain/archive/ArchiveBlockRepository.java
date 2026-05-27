package com.cravelog.api.domain.archive;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArchiveBlockRepository extends JpaRepository<ArchiveBlock, Long> {
    // 주인의 모든 아카이브를 시간 역순으로 조회
    List<ArchiveBlock> findByUserIdOrderByCreatedAtDesc(Long userId);
}

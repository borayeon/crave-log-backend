package com.cravelog.domain.record;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Long> {

    // 특정 유저의 모든 기록을 최신순으로 정렬하여 가져옵니다.
    // N+1 문제를 방지하기 위해 recordTags와 tag를 fetch join으로 한 번에 가져오는 예시입니다.
    @Query("SELECT DISTINCT r FROM Record r LEFT JOIN FETCH r.recordTags rt LEFT JOIN FETCH rt.tag WHERE r.user.id = :userId ORDER BY r.recordDate DESC")
    List<Record> findAllByUserIdWithTags(@Param("userId") Long userId);
}
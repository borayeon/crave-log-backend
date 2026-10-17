package com.cravelog.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyVisitRepository extends JpaRepository<DailyVisit, Long> {

    Optional<DailyVisit> findByUserIdAndVisitDate(Long userId, LocalDate visitDate);

    @Query("SELECT d FROM DailyVisit d WHERE d.user.id = :userId AND d.visitDate >= :startDate ORDER BY d.visitDate ASC")
    List<DailyVisit> findRecentVisits(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
}

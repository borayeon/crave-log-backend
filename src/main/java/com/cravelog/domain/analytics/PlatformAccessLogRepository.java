package com.cravelog.domain.analytics;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface PlatformAccessLogRepository extends JpaRepository<PlatformAccessLog, Long> {

    List<PlatformAccessLog> findTop30ByOrderByAccessedAtDesc();

    @Query("SELECT l.path, COUNT(l) as cnt FROM PlatformAccessLog l GROUP BY l.path ORDER BY cnt DESC")
    List<Object[]> findTopPaths(Pageable pageable);

    @Query("SELECT l.browser, COUNT(l) as cnt FROM PlatformAccessLog l GROUP BY l.browser ORDER BY cnt DESC")
    List<Object[]> findTopBrowsers(Pageable pageable);

    @Query("SELECT l.os, COUNT(l) as cnt FROM PlatformAccessLog l GROUP BY l.os ORDER BY cnt DESC")
    List<Object[]> findTopOs(Pageable pageable);

    @Query("SELECT l.referer, COUNT(l) as cnt FROM PlatformAccessLog l WHERE l.referer IS NOT NULL AND l.referer != '' GROUP BY l.referer ORDER BY cnt DESC")
    List<Object[]> findTopReferrers(Pageable pageable);

    @Query("SELECT l.eventDetail, COUNT(l) as cnt FROM PlatformAccessLog l WHERE l.eventType = 'SEARCH' AND l.eventDetail IS NOT NULL AND l.eventDetail != '' GROUP BY l.eventDetail ORDER BY cnt DESC")
    List<Object[]> findTopSearchKeywords(Pageable pageable);

    @Query("SELECT l.eventDetail, COUNT(l) as cnt FROM PlatformAccessLog l WHERE l.eventType = 'OUTBOUND_CLICK' AND l.eventDetail IS NOT NULL AND l.eventDetail != '' GROUP BY l.eventDetail ORDER BY cnt DESC")
    List<Object[]> findTopOutboundClicks(Pageable pageable);

    @Query("SELECT COALESCE(AVG(l.durationSeconds), 0.0) FROM PlatformAccessLog l WHERE l.durationSeconds > 0 AND l.durationSeconds < 3600")
    Double findAverageDurationSeconds();

    @Modifying
    @Transactional
    void deleteByAccessedAtBefore(LocalDateTime cutoff);
}

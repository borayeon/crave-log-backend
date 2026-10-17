package com.cravelog.domain.analytics;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlatformAccessLogRepository extends JpaRepository<PlatformAccessLog, Long> {

    List<PlatformAccessLog> findTop30ByOrderByAccessedAtDesc();

    @Query("SELECT l.path, COUNT(l) as cnt FROM PlatformAccessLog l GROUP BY l.path ORDER BY cnt DESC")
    List<Object[]> findTopPaths(Pageable pageable);

    @Query("SELECT l.browser, COUNT(l) as cnt FROM PlatformAccessLog l GROUP BY l.browser ORDER BY cnt DESC")
    List<Object[]> findTopBrowsers(Pageable pageable);

    @Query("SELECT l.os, COUNT(l) as cnt FROM PlatformAccessLog l GROUP BY l.os ORDER BY cnt DESC")
    List<Object[]> findTopOs(Pageable pageable);
}

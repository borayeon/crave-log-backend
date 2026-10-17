package com.cravelog.domain.analytics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PlatformDailyStatRepository extends JpaRepository<PlatformDailyStat, Long> {

    Optional<PlatformDailyStat> findByStatDate(LocalDate statDate);

    List<PlatformDailyStat> findByStatDateBetweenOrderByStatDateAsc(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(s.uniqueVisitors), 0) FROM PlatformDailyStat s")
    Long sumTotalUniqueVisitors();

    @Query("SELECT COALESCE(SUM(s.pageViews), 0) FROM PlatformDailyStat s")
    Long sumTotalPageViews();

    @Query("SELECT COALESCE(SUM(s.mobileCount), 0) FROM PlatformDailyStat s")
    Long sumTotalMobileCount();

    @Query("SELECT COALESCE(SUM(s.desktopCount), 0) FROM PlatformDailyStat s")
    Long sumTotalDesktopCount();

    @Query("SELECT COALESCE(SUM(s.tabletCount), 0) FROM PlatformDailyStat s")
    Long sumTotalTabletCount();
}

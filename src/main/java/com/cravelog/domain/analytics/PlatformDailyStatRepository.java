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

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE PlatformDailyStat s SET s.uniqueVisitors = s.uniqueVisitors + 1 WHERE s.statDate = :statDate")
    int incrementUniqueVisitor(@Param("statDate") LocalDate statDate);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE PlatformDailyStat s SET s.pageViews = s.pageViews + 1, " +
           "s.desktopCount = s.desktopCount + CASE WHEN :device = 'DESKTOP' THEN 1L ELSE 0L END, " +
           "s.mobileCount = s.mobileCount + CASE WHEN :device = 'MOBILE' THEN 1L ELSE 0L END, " +
           "s.tabletCount = s.tabletCount + CASE WHEN :device = 'TABLET' THEN 1L ELSE 0L END " +
           "WHERE s.statDate = :statDate")
    int incrementPageViewAndDevice(@Param("statDate") LocalDate statDate, @Param("device") String device);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE PlatformDailyStat s SET s.uniqueVisitors = s.uniqueVisitors + 1, s.pageViews = s.pageViews + 1, " +
           "s.desktopCount = s.desktopCount + CASE WHEN :device = 'DESKTOP' THEN 1L ELSE 0L END, " +
           "s.mobileCount = s.mobileCount + CASE WHEN :device = 'MOBILE' THEN 1L ELSE 0L END, " +
           "s.tabletCount = s.tabletCount + CASE WHEN :device = 'TABLET' THEN 1L ELSE 0L END " +
           "WHERE s.statDate = :statDate")
    int incrementUvAndPageViewAndDevice(@Param("statDate") LocalDate statDate, @Param("device") String device);
}

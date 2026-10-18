package com.cravelog.domain.analytics;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "platform_daily_stats", uniqueConstraints = {
        @UniqueConstraint(name = "uk_platform_stat_date", columnNames = {"stat_date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlatformDailyStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "unique_visitors", nullable = false)
    private Long uniqueVisitors = 0L;

    @Column(name = "page_views", nullable = false)
    private Long pageViews = 0L;

    @Column(name = "mobile_count", nullable = false)
    private Long mobileCount = 0L;

    @Column(name = "desktop_count", nullable = false)
    private Long desktopCount = 0L;

    @Column(name = "tablet_count", nullable = false)
    private Long tabletCount = 0L;

    public PlatformDailyStat(LocalDate statDate) {
        this.statDate = statDate;
        this.uniqueVisitors = 0L;
        this.pageViews = 0L;
        this.mobileCount = 0L;
        this.desktopCount = 0L;
        this.tabletCount = 0L;
    }

    public void incrementUniqueVisitor() {
        this.uniqueVisitors++;
    }

    public void incrementPageView() {
        this.pageViews++;
    }

    public void incrementDevice(String deviceType) {
        if ("MOBILE".equalsIgnoreCase(deviceType)) {
            this.mobileCount++;
        } else if ("TABLET".equalsIgnoreCase(deviceType)) {
            this.tabletCount++;
        } else {
            this.desktopCount++;
        }
    }
}

package com.cravelog.domain.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformOverviewDto {
    private long todayUv;
    private long yesterdayUv;
    private long todayPv;
    private long yesterdayPv;
    private long totalUv;
    private long totalPv;
    private long totalUsers;
    private long totalRecords;

    private List<DailyTrafficDto> recentDailyStats;
    private List<NameValueDto> deviceShare;
    private List<NameValueDto> topBrowsers;
    private List<NameValueDto> topOs;
    private List<NameValueDto> topPaths;
    private List<AccessLogDto> recentLogs;
}

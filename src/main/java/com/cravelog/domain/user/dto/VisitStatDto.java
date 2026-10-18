package com.cravelog.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class VisitStatDto {

    @Getter
    @Builder
    public static class Response {
        private Long todayVisits;
        private Long yesterdayVisits;
        private Long totalVisits;
        private Long highestVisits; // 최고 방문일 수치
        private List<DailyStat> recentDays;
    }

    @Getter
    @Builder
    public static class DailyStat {
        private String date;       // 예: 2026.10.17
        private String dayLabel;   // 예: 10/17
        private long count;
    }
}

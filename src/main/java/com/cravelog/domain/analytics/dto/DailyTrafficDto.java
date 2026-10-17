package com.cravelog.domain.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTrafficDto {
    private String date;        // "2026.10.17"
    private String dayLabel;    // "10/17"
    private long uv;            // 순 방문자 수 (DAU)
    private long pv;            // 페이지뷰 / API 요청 수
    private long mobileCount;
    private long desktopCount;
}

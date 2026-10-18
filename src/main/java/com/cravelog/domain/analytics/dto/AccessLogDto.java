package com.cravelog.domain.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessLogDto {
    private Long id;
    private String ipMasked;
    private String path;
    private String deviceType;
    private String browser;
    private String os;
    private String timeAgo;
    private String accessedAt;
}

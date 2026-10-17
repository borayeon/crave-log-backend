package com.cravelog.domain.analytics.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TrackVisitRequest {
    private String path;
    private String referer;
}

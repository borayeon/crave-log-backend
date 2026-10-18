package com.cravelog.domain.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NameValueDto {
    private String name;
    private long value;
    private double percentage;
}

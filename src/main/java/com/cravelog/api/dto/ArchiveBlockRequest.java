package com.cravelog.api.dto;

import com.cravelog.api.domain.enums.SpaceType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArchiveBlockRequest {
    private String title;
    private String content;
    private String url;
    private String type;
    private SpaceType spaceType;
}

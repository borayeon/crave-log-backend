package com.cravelog.domain.analytics;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "platform_access_logs", indexes = {
        @Index(name = "idx_access_time", columnList = "accessed_at DESC")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlatformAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_masked", length = 50)
    private String ipMasked;

    @Column(name = "path", length = 100)
    private String path;

    @Column(name = "device_type", length = 20)
    private String deviceType;

    @Column(name = "browser", length = 50)
    private String browser;

    @Column(name = "os", length = 50)
    private String os;

    @Column(name = "referer", length = 255)
    private String referer;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "event_type", length = 30)
    private String eventType;

    @Column(name = "event_detail", length = 255)
    private String eventDetail;

    @Column(name = "accessed_at", nullable = false)
    private LocalDateTime accessedAt;

    @Builder
    public PlatformAccessLog(String ipMasked, String path, String deviceType, String browser, String os, String referer, Integer durationSeconds, String eventType, String eventDetail, LocalDateTime accessedAt) {
        this.ipMasked = ipMasked;
        this.path = path;
        this.deviceType = deviceType;
        this.browser = browser;
        this.os = os;
        this.referer = referer;
        this.durationSeconds = durationSeconds != null ? durationSeconds : 0;
        this.eventType = eventType != null ? eventType : "PAGE_VIEW";
        this.eventDetail = eventDetail;
        this.accessedAt = accessedAt != null ? accessedAt : LocalDateTime.now();
    }
}

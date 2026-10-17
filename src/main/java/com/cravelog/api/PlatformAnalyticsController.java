package com.cravelog.api;

import com.cravelog.domain.analytics.PlatformAnalyticsService;
import com.cravelog.domain.analytics.dto.PlatformOverviewDto;
import com.cravelog.domain.analytics.dto.TrackVisitRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class PlatformAnalyticsController {

    private final PlatformAnalyticsService platformAnalyticsService;

    /**
     * 프론트엔드 라우트 이동 및 초기 진입 시 비동기 트래픽 비콘 수신
     */
    @PostMapping("/track")
    public ResponseEntity<Void> trackVisit(@RequestBody(required = false) TrackVisitRequest request,
                                           HttpServletRequest httpRequest) {
        String clientIp = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String path = (request != null && request.getPath() != null) ? request.getPath() : "/";
        String referer = (request != null && request.getReferer() != null) ? request.getReferer() : httpRequest.getHeader("Referer");

        platformAnalyticsService.recordPlatformVisit(clientIp, userAgent, path, referer);
        return ResponseEntity.ok().build();
    }

    /**
     * 개발자/관리자 플랫폼 트래픽 종합 대시보드 데이터 조회
     */
    @GetMapping("/overview")
    public ResponseEntity<PlatformOverviewDto> getOverview(@RequestParam(defaultValue = "14") int days) {
        int targetDays = Math.min(Math.max(days, 7), 60);
        PlatformOverviewDto overview = platformAnalyticsService.getOverview(targetDays);
        return ResponseEntity.ok(overview);
    }

    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("CF-Connecting-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

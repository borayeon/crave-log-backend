package com.cravelog.api;

import com.cravelog.domain.analytics.PlatformAnalyticsService;
import com.cravelog.domain.analytics.dto.PlatformOverviewDto;
import com.cravelog.domain.analytics.dto.TrackVisitRequest;
import com.cravelog.domain.user.UserRepository;
import com.cravelog.domain.user.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PlatformAnalyticsController {

    private final PlatformAnalyticsService platformAnalyticsService;
    private final UserRepository userRepository;

    /**
     * 프론트엔드 라우트 이동 및 초기 진입 시 비동기 트래픽 비콘 수신 (퍼블릭 수신)
     */
    @PostMapping("/analytics/track")
    public ResponseEntity<Void> trackVisit(@RequestBody(required = false) TrackVisitRequest request,
                                           HttpServletRequest httpRequest) {
        String clientIp = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String path = (request != null && request.getPath() != null) ? request.getPath() : "/";
        String referer = (request != null && request.getReferer() != null) ? request.getReferer() : httpRequest.getHeader("Referer");
        Integer duration = (request != null) ? request.getDurationSeconds() : null;
        String eventType = (request != null && request.getEventType() != null) ? request.getEventType() : "PAGE_VIEW";
        String eventDetail = (request != null) ? request.getEventDetail() : null;

        platformAnalyticsService.recordPlatformVisit(clientIp, userAgent, path, referer, duration, eventType, eventDetail);
        return ResponseEntity.ok().build();
    }

    /**
     * 🛡️ 개발자/관리자 전용 대시보드 API (IP 화이트리스트 + ROLE_ADMIN 이중 방어)
     */
    @GetMapping({"/admin/analytics/overview", "/analytics/overview"})
    public ResponseEntity<PlatformOverviewDto> getOverview(
            @AuthenticationPrincipal User principal,
            @RequestParam(defaultValue = "14") int days,
            HttpServletRequest request) {

        String clientIp = extractClientIp(request);

        // 1. IP 화이트리스트 검증 (로컬망 및 지정 IP만 허용)
        if (!isWhitelistedIp(clientIp)) {
            log.warn("🚨 비허가 외부 IP에서 관리자 API 접근 시도 차단: IP={}", clientIp);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "외부 IP에서의 관리자 API 접근이 엄격히 차단되었습니다.");
        }

        // 2. 관리자 토큰 및 권한 검증 (ROLE_ADMIN)
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "관리자 로그인이 필요합니다.");
        }

        Long userId = Long.parseLong(principal.getUsername());
        com.cravelog.domain.user.User userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        if (userEntity.getUserRole() != UserRole.ROLE_ADMIN) {
            log.warn("🚨 비관리자 계정의 관리자 API 접근 시도 차단: User={}", userEntity.getHandle());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "최고 관리자(ROLE_ADMIN) 권한이 필요합니다.");
        }

        int targetDays = Math.min(Math.max(days, 7), 60);
        PlatformOverviewDto overview = platformAnalyticsService.getOverview(targetDays);
        return ResponseEntity.ok(overview);
    }

    private boolean isWhitelistedIp(String ip) {
        if (ip == null || ip.isBlank()) return false;
        // 로컬호스트 루프백 및 내부망 허용
        return ip.equals("127.0.0.1") ||
               ip.equals("0:0:0:0:0:0:0:1") ||
               ip.equalsIgnoreCase("localhost") ||
               ip.startsWith("192.168.") ||
               ip.startsWith("10.");
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

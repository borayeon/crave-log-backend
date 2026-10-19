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

        String remoteAddr = request.getRemoteAddr();
        String clientIp = extractClientIp(request);

        // 1. IP 화이트리스트 검증 (외부에서의 X-Forwarded-For 헤더 스푸핑 원천 차단: 실제 소켓 연결 IP도 신뢰망이어야 함)
        if (!isWhitelistedIp(remoteAddr) || !isWhitelistedIp(clientIp)) {
            log.warn("🚨 비허가 외부 IP에서 관리자 API 접근 시도 차단: remoteAddr={}, clientIp={}", remoteAddr, clientIp);
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
               ip.equals("::1") ||
               ip.equalsIgnoreCase("localhost") ||
               ip.startsWith("192.168.") ||
               ip.startsWith("172.") ||
               ip.startsWith("10.");
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String remoteAddr = request.getRemoteAddr();
        // 신뢰할 수 있는 내부 프록시(루프백/사설망)를 거친 요청일 때만 X-Forwarded-For 헤더 참조
        if (isWhitelistedIp(remoteAddr)) {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("CF-Connecting-IP");
            }
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.contains(",") ? ip.split(",")[0].trim() : ip.trim();
            }
        }
        return remoteAddr;
    }
}

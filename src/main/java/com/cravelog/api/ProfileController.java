package com.cravelog.api;

import com.cravelog.domain.user.UserService;
import com.cravelog.domain.user.dto.ProfileDto;
import com.cravelog.domain.user.dto.VisitStatDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    /**
     * 1. 누구나 볼 수 있는 퍼블릭 프로필 조회 (⭐️ 페르소나 파라미터 및 방문자 추적 적용)
     * GET /api/v1/users/{handle}/profile?p={personaId}
     */
    @GetMapping("/users/{handle}/profile")
    public ResponseEntity<ProfileDto.Response> getPublicProfile(
            @PathVariable String handle,
            @RequestParam(name = "p", required = false) String personaId,
            @AuthenticationPrincipal User principal,
            HttpServletRequest request
    ) {
        String visitorIp = extractClientIp(request);
        Long authUserId = (principal != null) ? Long.parseLong(principal.getUsername()) : null;

        ProfileDto.Response response = userService.getPublicProfile(handle, personaId, visitorIp, authUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * 1-1. 특정 유저의 방문자 통계 조회 (대시보드 / 7일 추이 차트용)
     * GET /api/v1/users/{handle}/visit-stats
     */
    @GetMapping("/users/{handle}/visit-stats")
    public ResponseEntity<VisitStatDto.Response> getVisitStats(@PathVariable String handle) {
        VisitStatDto.Response response = userService.getVisitStatsByHandle(handle);
        return ResponseEntity.ok(response);
    }

    /**
     * 1-2. 프로필 서브 탭 / 페르소나 PIN 서버 사이드 검증 API
     * POST /api/v1/users/{handle}/verify-pin
     */
    @PostMapping("/users/{handle}/verify-pin")
    public ResponseEntity<ProfileDto.Response> verifyProfilePin(
            @PathVariable String handle,
            @RequestBody ProfileDto.VerifyPinRequest requestDto,
            @AuthenticationPrincipal User principal,
            HttpServletRequest request
    ) {
        String clientIp = extractClientIp(request);
        Long authUserId = (principal != null) ? Long.parseLong(principal.getUsername()) : null;
        ProfileDto.Response response = userService.verifyProfilePin(handle, requestDto, authUserId, clientIp);
        return ResponseEntity.ok(response);
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String remoteAddr = request.getRemoteAddr();
        // 신뢰된 내부 리버스 프록시에서 온 요청일 때만 X-Forwarded-For / CF-Connecting-IP 신뢰 (IP 스푸핑 통한 방문수 조작 방어)
        boolean trustedProxy = remoteAddr != null && (
                "127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr) || "::1".equals(remoteAddr)
                || remoteAddr.startsWith("10.") || remoteAddr.startsWith("192.168.")
                || remoteAddr.matches("^172\\.(1[6-9]|2[0-9]|3[0-1])\\..*")
        );
        if (trustedProxy) {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("CF-Connecting-IP");
            }
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.contains(",") ? ip.split(",")[0].trim() : ip.trim();
            }
        }
        return remoteAddr != null ? remoteAddr : "unknown";
    }

    /**
     * 2. 유저 검색 (검색 노출 설정, 필터 모드 및 팔로우 상태 반영)
     */
    @GetMapping("/users/search")
    public ResponseEntity<List<ProfileDto.SearchResponse>> searchUsers(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "all") String filter,
            @AuthenticationPrincipal User principal
    ) {
        Long authUserId = (principal != null) ? Long.parseLong(principal.getUsername()) : null;
        List<ProfileDto.SearchResponse> response = userService.searchUsers(keyword, filter, authUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * 3. 내 프로필 조회 (마이페이지용)
     */
    @GetMapping("/me/profile")
    public ResponseEntity<ProfileDto.Response> getMyProfile(@AuthenticationPrincipal User principal) {
        Long myUserId = Long.parseLong(principal.getUsername());
        ProfileDto.Response response = userService.getMyProfile(myUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * 4. 내 프로필 수정
     */
    @PutMapping("/me/profile")
    public ResponseEntity<Void> updateProfile(@AuthenticationPrincipal User principal, @RequestBody ProfileDto.UpdateRequest request) {
        Long myUserId = Long.parseLong(principal.getUsername());
        userService.updateProfile(myUserId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 4-1. 내 검색 노출 설정 단독 변경 API
     */
    @PatchMapping("/me/search-privacy")
    public ResponseEntity<Void> updateSearchPrivacy(
            @AuthenticationPrincipal User principal,
            @RequestBody ProfileDto.SearchPrivacyRequest request
    ) {
        Long myUserId = Long.parseLong(principal.getUsername());
        userService.updateSearchPrivacy(myUserId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 5. 비밀번호 변경 API
     */
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal User principal, @RequestBody ProfileDto.ChangePasswordRequest request) {
        Long myUserId = Long.parseLong(principal.getUsername());
        userService.changePassword(myUserId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 6. 계정 탈퇴 API
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal User principal, 
            @RequestBody(required = false) ProfileDto.DeleteAccountRequest request) {
        Long myUserId = Long.parseLong(principal.getUsername());
        userService.deleteAccount(myUserId, request);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}
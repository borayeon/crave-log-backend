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

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("CF-Connecting-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 2. 유저 검색
     */
    @GetMapping("/users/search")
    public ResponseEntity<List<ProfileDto.Response>> searchUsers(@RequestParam(required = false, defaultValue = "") String keyword) {
        List<ProfileDto.Response> response = userService.searchUsers(keyword);
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
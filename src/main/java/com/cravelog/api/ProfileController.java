package com.cravelog.api;

import com.cravelog.domain.user.UserService;
import com.cravelog.domain.user.dto.ProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    /**
     * 1. 누구나 볼 수 있는 퍼블릭 프로필 조회 (비공개 데이터 필터링됨)
     * GET /api/v1/users/{handle}/profile
     */
    @GetMapping("/users/{handle}/profile")
    public ResponseEntity<ProfileDto.Response> getPublicProfile(@PathVariable String handle) {
        ProfileDto.Response response = userService.getPublicProfile(handle);
        return ResponseEntity.ok(response);
    }

    /**
     * 2. 유저 검색 (keyword 파라미터가 없거나 빈칸이면 전체 유저 반환)
     * GET /api/v1/users/search?keyword=
     */
    @GetMapping("/users/search")
    public ResponseEntity<List<ProfileDto.Response>> searchUsers(@RequestParam(required = false, defaultValue = "") String keyword) {
        List<ProfileDto.Response> response = userService.searchUsers(keyword);
        return ResponseEntity.ok(response);
    }

    /**
     * 3. 내 프로필 조회 (마이페이지용 - 비공개 데이터 포함)
     * GET /api/v1/me/profile
     */
    @GetMapping("/me/profile")
    public ResponseEntity<ProfileDto.Response> getMyProfile(@AuthenticationPrincipal User principal) {
        Long myUserId = Long.parseLong(principal.getUsername());
        ProfileDto.Response response = userService.getMyProfile(myUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * 4. 내 프로필 수정
     * PUT /api/v1/me/profile
     */
    @PutMapping("/me/profile")
    public ResponseEntity<Void> updateProfile(@AuthenticationPrincipal User principal, @RequestBody ProfileDto.UpdateRequest request) {
        Long myUserId = Long.parseLong(principal.getUsername());
        userService.updateProfile(myUserId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 5. ⭐️ 비밀번호 변경 API
     */
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal User principal, @RequestBody ProfileDto.ChangePasswordRequest request) {
        Long myUserId = Long.parseLong(principal.getUsername());
        userService.changePassword(myUserId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 6. ⭐️ 계정 탈퇴 API
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal User principal, @RequestBody ProfileDto.DeleteAccountRequest request) {
        Long myUserId = Long.parseLong(principal.getUsername());
        userService.deleteAccount(myUserId, request);
        return ResponseEntity.ok().build();
    }
}
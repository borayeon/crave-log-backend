package com.cravelog.api;

import com.cravelog.domain.user.UserService;
import com.cravelog.domain.user.dto.ProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    /**
     * 1. 누구나 볼 수 있는 퍼블릭 프로필 조회 (비공개 데이터 필터링됨)
     * GET /api/v1/users/taekyeong.dev/profile
     */
    @GetMapping("/users/{handle}/profile")
    public ResponseEntity<ProfileDto.Response> getPublicProfile(@PathVariable String handle) {
        ProfileDto.Response response = userService.getPublicProfile(handle);
        return ResponseEntity.ok(response);
    }

    /**
     * 2. 내 프로필 조회 (비공개 데이터 포함)
     * GET /api/v1/me/profile
     * (현재는 로그인이 없으므로 강제로 1번 유저라고 가정)
     */
    @GetMapping("/me/profile")
    public ResponseEntity<ProfileDto.Response> getMyProfile() {
        Long myUserId = 1L; // TODO: 추후 Spring Security 연동 후 로그인 유저 ID로 변경
        ProfileDto.Response response = userService.getMyProfile(myUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * 3. 내 프로필 수정
     * PUT /api/v1/me/profile
     */
    @PutMapping("/me/profile")
    public ResponseEntity<Void> updateMyProfile(@RequestBody ProfileDto.UpdateRequest request) {
        Long myUserId = 1L; // TODO: 추후 Spring Security 연동 후 로그인 유저 ID로 변경
        userService.updateProfile(myUserId, request);
        return ResponseEntity.ok().build();
    }
}
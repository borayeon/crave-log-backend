package com.cravelog.api;

import com.cravelog.domain.user.FollowService;
import com.cravelog.domain.user.dto.FollowDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * 특정 사용자 팔로우하기
     */
    @PostMapping("/users/{handle}/follow")
    public ResponseEntity<Void> follow(@AuthenticationPrincipal User principal, @PathVariable String handle) {
        Long myUserId = Long.parseLong(principal.getUsername());
        followService.follow(myUserId, handle);
        return ResponseEntity.ok().build();
    }

    /**
     * 특정 사용자 언팔로우하기
     */
    @DeleteMapping("/users/{handle}/follow")
    public ResponseEntity<Void> unfollow(@AuthenticationPrincipal User principal, @PathVariable String handle) {
        Long myUserId = Long.parseLong(principal.getUsername());
        followService.unfollow(myUserId, handle);
        return ResponseEntity.ok().build();
    }

    /**
     * 나를 팔로우한 특정 팔로워 삭제(끊기)
     */
    @DeleteMapping("/me/followers/{followerId}")
    public ResponseEntity<Void> removeFollower(@AuthenticationPrincipal User principal, @PathVariable Long followerId) {
        Long myUserId = Long.parseLong(principal.getUsername());
        followService.removeFollower(myUserId, followerId);
        return ResponseEntity.ok().build();
    }

    /**
     * 내 팔로워 목록 조회
     */
    @GetMapping("/me/followers")
    public ResponseEntity<List<FollowDto.UserSummary>> getMyFollowers(@AuthenticationPrincipal User principal) {
        Long myUserId = Long.parseLong(principal.getUsername());
        List<FollowDto.UserSummary> followers = followService.getFollowers(myUserId);
        return ResponseEntity.ok(followers);
    }

    /**
     * 내가 팔로우하는 목록 조회
     */
    @GetMapping("/me/followings")
    public ResponseEntity<List<FollowDto.UserSummary>> getMyFollowings(@AuthenticationPrincipal User principal) {
        Long myUserId = Long.parseLong(principal.getUsername());
        List<FollowDto.UserSummary> followings = followService.getFollowings(myUserId);
        return ResponseEntity.ok(followings);
    }

    /**
     * 특정 대상에 대한 팔로우 여부 및 카운트 조회
     */
    @GetMapping("/users/{handle}/follow-status")
    public ResponseEntity<FollowDto.FollowStatusResponse> getFollowStatus(
            @AuthenticationPrincipal User principal,
            @PathVariable String handle) {
        Long myUserId = principal != null ? Long.parseLong(principal.getUsername()) : null;
        FollowDto.FollowStatusResponse response = followService.getFollowStatus(myUserId, handle);
        return ResponseEntity.ok(response);
    }
}

package com.cravelog.api;

import com.cravelog.domain.user.FollowService;
import com.cravelog.domain.user.dto.FollowDto;
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
public class FollowController {

    private final FollowService followService;

    /**
     * 특정 사용자에게 팔로우 신청(Request) 보내기
     */
    @PostMapping("/users/{handle}/follow")
    public ResponseEntity<FollowDto.FollowStatusResponse> follow(@AuthenticationPrincipal User principal, @PathVariable String handle) {
        Long myUserId = Long.parseLong(principal.getUsername());
        FollowDto.FollowStatusResponse response = followService.follow(myUserId, handle);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 사용자 언팔로우하기 또는 팔로우 신청 취소하기
     */
    @DeleteMapping("/users/{handle}/follow")
    public ResponseEntity<FollowDto.FollowStatusResponse> unfollow(@AuthenticationPrincipal User principal, @PathVariable String handle) {
        Long myUserId = Long.parseLong(principal.getUsername());
        FollowDto.FollowStatusResponse response = followService.unfollow(myUserId, handle);
        return ResponseEntity.ok(response);
    }

    /**
     * 나에게 들어온 팔로우 신청 대기 목록 조회
     */
    @GetMapping("/me/follow-requests")
    public ResponseEntity<List<FollowDto.UserSummary>> getFollowRequests(@AuthenticationPrincipal User principal) {
        Long myUserId = Long.parseLong(principal.getUsername());
        List<FollowDto.UserSummary> requests = followService.getPendingRequests(myUserId);
        return ResponseEntity.ok(requests);
    }

    /**
     * 나에게 들어온 팔로우 신청 대기 건수
     */
    @GetMapping("/me/follow-requests/count")
    public ResponseEntity<Map<String, Long>> getFollowRequestCount(@AuthenticationPrincipal User principal) {
        Long myUserId = Long.parseLong(principal.getUsername());
        long count = followService.getPendingRequestCount(myUserId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * 팔로우 신청 수락하기
     */
    @PostMapping("/me/follow-requests/{followerId}/accept")
    public ResponseEntity<Void> acceptFollowRequest(
            @AuthenticationPrincipal User principal,
            @PathVariable Long followerId
    ) {
        Long myUserId = Long.parseLong(principal.getUsername());
        followService.acceptFollowRequest(myUserId, followerId);
        return ResponseEntity.ok().build();
    }

    /**
     * 팔로우 신청 거절하기
     */
    @DeleteMapping("/me/follow-requests/{followerId}/reject")
    public ResponseEntity<Void> rejectFollowRequest(
            @AuthenticationPrincipal User principal,
            @PathVariable Long followerId
    ) {
        Long myUserId = Long.parseLong(principal.getUsername());
        followService.rejectFollowRequest(myUserId, followerId);
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
     * 내 팔로워 목록 조회 (수락 완료된 팔로워만)
     */
    @GetMapping("/me/followers")
    public ResponseEntity<List<FollowDto.UserSummary>> getMyFollowers(@AuthenticationPrincipal User principal) {
        Long myUserId = Long.parseLong(principal.getUsername());
        List<FollowDto.UserSummary> followers = followService.getFollowers(myUserId);
        return ResponseEntity.ok(followers);
    }

    /**
     * 내가 팔로우하는 목록 조회 (수락 완료된 팔로잉만)
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

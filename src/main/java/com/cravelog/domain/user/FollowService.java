package com.cravelog.domain.user;

import com.cravelog.domain.notification.NotificationService;
import com.cravelog.domain.user.dto.FollowDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * 특정 사용자에게 팔로우 신청(Request) 보내기
     */
    @Transactional
    public FollowDto.FollowStatusResponse follow(Long followerId, String targetHandle) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        User following = userRepository.findByHandle(targetHandle)
                .orElseThrow(() -> new IllegalArgumentException("대상 사용자를 찾을 수 없습니다."));

        if (following.getUserRole() == UserRole.ROLE_ADMIN) {
            throw new IllegalArgumentException("대상 사용자를 찾을 수 없습니다.");
        }

        if (follower.getId().equals(following.getId())) {
            throw new IllegalArgumentException("자신을 팔로우할 수 없습니다.");
        }

        Optional<Follow> existing = followRepository.findByFollowerIdAndFollowingId(follower.getId(), following.getId());
        if (existing.isEmpty()) {
            // 신규 팔로우 신청 (PENDING 상태로 등록)
            Follow follow = new Follow(follower, following, FollowStatus.PENDING);
            followRepository.save(follow);
            // 상대방에게 팔로우 신청 알림 발송
            notificationService.createFollowRequestNotification(following, follower);
        }

        return getFollowStatus(followerId, targetHandle);
    }

    /**
     * 언팔로우 또는 팔로우 신청 취소하기
     */
    @Transactional
    public FollowDto.FollowStatusResponse unfollow(Long followerId, String targetHandle) {
        User following = userRepository.findByHandle(targetHandle)
                .orElseThrow(() -> new IllegalArgumentException("대상 사용자를 찾을 수 없습니다."));

        if (following.getUserRole() == UserRole.ROLE_ADMIN) {
            throw new IllegalArgumentException("대상 사용자를 찾을 수 없습니다.");
        }

        followRepository.deleteByFollowerIdAndFollowingId(followerId, following.getId());

        return getFollowStatus(followerId, targetHandle);
    }

    /**
     * 팔로우 신청 수락하기 (Accept)
     */
    @Transactional
    public void acceptFollowRequest(Long currentUserId, Long targetFollowerId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(targetFollowerId, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우 신청을 찾을 수 없습니다."));

        if (follow.getStatus() != FollowStatus.PENDING) {
            return;
        }

        follow.accept();
        // 신청을 보냈던 사람에게 수락 알림 발송
        notificationService.createFollowAcceptNotification(follow.getFollower(), follow.getFollowing());
    }

    /**
     * 팔로우 신청 거절하기 (Reject)
     */
    @Transactional
    public void rejectFollowRequest(Long currentUserId, Long targetFollowerId) {
        followRepository.deleteByFollowerIdAndFollowingId(targetFollowerId, currentUserId);
    }

    /**
     * 나에게 들어온 팔로우 신청 대기 목록 조회
     */
    @Transactional(readOnly = true)
    public List<FollowDto.UserSummary> getPendingRequests(Long currentUserId) {
        List<Follow> pendingFollows = followRepository.findAllPendingRequestsByFollowingId(currentUserId);
        return pendingFollows.stream()
                .map(f -> {
                    User u = f.getFollower();
                    return FollowDto.UserSummary.builder()
                            .id(u.getId())
                            .handle(u.getHandle())
                            .name(u.getName())
                            .profileImageUrl(u.getProfileImageUrl())
                            .statusMessage(u.getStatusMessage())
                            .isFollowing(false)
                            .isPending(true)
                            .followId(f.getId())
                            .requestedAt(f.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 나에게 들어온 팔로우 신청 대기 건수
     */
    @Transactional(readOnly = true)
    public long getPendingRequestCount(Long currentUserId) {
        return followRepository.countByFollowingIdAndStatus(currentUserId, FollowStatus.PENDING);
    }

    /**
     * 나를 팔로우한 특정 팔로워 삭제(끊기)
     */
    @Transactional
    public void removeFollower(Long currentUserId, Long targetFollowerId) {
        followRepository.deleteByFollowerIdAndFollowingId(targetFollowerId, currentUserId);
    }

    /**
     * 수락 완료된 내 팔로워 목록 조회
     */
    @Transactional(readOnly = true)
    public List<FollowDto.UserSummary> getFollowers(Long currentUserId) {
        List<Follow> follows = followRepository.findAllByFollowingIdWithFollower(currentUserId);
        return follows.stream()
                .map(f -> {
                    User u = f.getFollower();
                    boolean isMutual = followRepository.existsByFollowerIdAndFollowingIdAndStatus(currentUserId, u.getId(), FollowStatus.ACCEPTED);
                    return FollowDto.UserSummary.builder()
                            .id(u.getId())
                            .handle(u.getHandle())
                            .name(u.getName())
                            .profileImageUrl(u.getProfileImageUrl())
                            .statusMessage(u.getStatusMessage())
                            .isFollowing(isMutual)
                            .isPending(false)
                            .followId(f.getId())
                            .requestedAt(f.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 수락 완료되어 내가 팔로우하는 목록 조회
     */
    @Transactional(readOnly = true)
    public List<FollowDto.UserSummary> getFollowings(Long currentUserId) {
        List<Follow> follows = followRepository.findAllByFollowerIdWithFollowing(currentUserId);
        return follows.stream()
                .map(f -> {
                    User u = f.getFollowing();
                    return FollowDto.UserSummary.builder()
                            .id(u.getId())
                            .handle(u.getHandle())
                            .name(u.getName())
                            .profileImageUrl(u.getProfileImageUrl())
                            .statusMessage(u.getStatusMessage())
                            .isFollowing(true)
                            .isPending(false)
                            .followId(f.getId())
                            .requestedAt(f.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 대상에 대한 팔로우 여부 및 카운트 조회 (신청 대기 여부 isPending 포함)
     */
    @Transactional(readOnly = true)
    public FollowDto.FollowStatusResponse getFollowStatus(Long currentUserId, String targetHandle) {
        User target = userRepository.findByHandle(targetHandle)
                .orElseThrow(() -> new IllegalArgumentException("대상 사용자를 찾을 수 없습니다."));

        boolean isFollowing = false;
        boolean isPending = false;

        if (currentUserId != null) {
            Optional<Follow> followOpt = followRepository.findByFollowerIdAndFollowingId(currentUserId, target.getId());
            if (followOpt.isPresent()) {
                FollowStatus status = followOpt.get().getStatus();
                if (status == FollowStatus.ACCEPTED) {
                    isFollowing = true;
                } else if (status == FollowStatus.PENDING) {
                    isPending = true;
                }
            }
        }

        long followerCount = followRepository.countByFollowingIdAndStatus(target.getId(), FollowStatus.ACCEPTED);
        long followingCount = followRepository.countByFollowerIdAndStatus(target.getId(), FollowStatus.ACCEPTED);

        return FollowDto.FollowStatusResponse.builder()
                .isFollowing(isFollowing)
                .isPending(isPending)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .build();
    }
}

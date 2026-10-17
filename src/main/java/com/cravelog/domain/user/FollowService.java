package com.cravelog.domain.user;

import com.cravelog.domain.user.dto.FollowDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public void follow(Long followerId, String targetHandle) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        User following = userRepository.findByHandle(targetHandle)
                .orElseThrow(() -> new IllegalArgumentException("대상 사용자를 찾을 수 없습니다."));

        if (follower.getId().equals(following.getId())) {
            throw new IllegalArgumentException("자신을 팔로우할 수 없습니다.");
        }

        if (!followRepository.existsByFollowerIdAndFollowingId(follower.getId(), following.getId())) {
            Follow follow = new Follow(follower, following);
            followRepository.save(follow);
        }
    }

    @Transactional
    public void unfollow(Long followerId, String targetHandle) {
        User following = userRepository.findByHandle(targetHandle)
                .orElseThrow(() -> new IllegalArgumentException("대상 사용자를 찾을 수 없습니다."));

        followRepository.deleteByFollowerIdAndFollowingId(followerId, following.getId());
    }

    @Transactional
    public void removeFollower(Long currentUserId, Long targetFollowerId) {
        // 나(currentUserId)를 following으로 둔 follow 관계 삭제
        followRepository.deleteByFollowerIdAndFollowingId(targetFollowerId, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<FollowDto.UserSummary> getFollowers(Long currentUserId) {
        List<Follow> follows = followRepository.findAllByFollowingIdWithFollower(currentUserId);
        return follows.stream()
                .map(f -> {
                    User u = f.getFollower();
                    boolean isMutual = followRepository.existsByFollowerIdAndFollowingId(currentUserId, u.getId());
                    return FollowDto.UserSummary.builder()
                            .id(u.getId())
                            .handle(u.getHandle())
                            .name(u.getName())
                            .profileImageUrl(u.getProfileImageUrl())
                            .statusMessage(u.getStatusMessage())
                            .isFollowing(isMutual)
                            .build();
                })
                .collect(Collectors.toList());
    }

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
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FollowDto.FollowStatusResponse getFollowStatus(Long currentUserId, String targetHandle) {
        User target = userRepository.findByHandle(targetHandle)
                .orElseThrow(() -> new IllegalArgumentException("대상 사용자를 찾을 수 없습니다."));

        boolean isFollowing = false;
        if (currentUserId != null) {
            isFollowing = followRepository.existsByFollowerIdAndFollowingId(currentUserId, target.getId());
        }

        long followerCount = followRepository.countByFollowingId(target.getId());
        long followingCount = followRepository.countByFollowerId(target.getId());

        return FollowDto.FollowStatusResponse.builder()
                .isFollowing(isFollowing)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .build();
    }
}

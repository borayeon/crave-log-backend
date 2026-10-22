package com.cravelog.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowerIdAndFollowingIdAndStatus(Long followerId, Long followingId, FollowStatus status);

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // 수락된 팔로잉 목록
    @Query("SELECT f FROM Follow f JOIN FETCH f.following WHERE f.follower.id = :followerId AND f.status = com.cravelog.domain.user.FollowStatus.ACCEPTED ORDER BY f.createdAt DESC")
    List<Follow> findAllByFollowerIdWithFollowing(@Param("followerId") Long followerId);

    // 수락된 팔로워 목록
    @Query("SELECT f FROM Follow f JOIN FETCH f.follower WHERE f.following.id = :followingId AND f.status = com.cravelog.domain.user.FollowStatus.ACCEPTED ORDER BY f.createdAt DESC")
    List<Follow> findAllByFollowingIdWithFollower(@Param("followingId") Long followingId);

    // 나에게 들어온 팔로우 신청 대기 목록
    @Query("SELECT f FROM Follow f JOIN FETCH f.follower WHERE f.following.id = :followingId AND f.status = com.cravelog.domain.user.FollowStatus.PENDING ORDER BY f.createdAt DESC")
    List<Follow> findAllPendingRequestsByFollowingId(@Param("followingId") Long followingId);

    long countByFollowerIdAndStatus(Long followerId, FollowStatus status);

    long countByFollowingIdAndStatus(Long followingId, FollowStatus status);

    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :followerId AND f.status = com.cravelog.domain.user.FollowStatus.ACCEPTED")
    Set<Long> findFollowingIdsByFollowerId(@Param("followerId") Long followerId);

    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :followerId AND f.status = com.cravelog.domain.user.FollowStatus.PENDING")
    Set<Long> findPendingFollowingIdsByFollowerId(@Param("followerId") Long followerId);

    @Query("SELECT f.following.id, COUNT(f) FROM Follow f WHERE f.status = com.cravelog.domain.user.FollowStatus.ACCEPTED GROUP BY f.following.id")
    List<Object[]> countFollowersGroupByFollowingId();

    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    @Modifying
    @Query("DELETE FROM Follow f WHERE f.follower.id = :userId OR f.following.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}

package com.cravelog.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    @Query("SELECT f FROM Follow f JOIN FETCH f.following WHERE f.follower.id = :followerId ORDER BY f.createdAt DESC")
    List<Follow> findAllByFollowerIdWithFollowing(@Param("followerId") Long followerId);

    @Query("SELECT f FROM Follow f JOIN FETCH f.follower WHERE f.following.id = :followingId ORDER BY f.createdAt DESC")
    List<Follow> findAllByFollowingIdWithFollower(@Param("followingId") Long followingId);

    long countByFollowerId(Long followerId);

    long countByFollowingId(Long followingId);

    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);
}

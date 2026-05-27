package com.cravelog.api.domain.relationship;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RelationshipRepository extends JpaRepository<Relationship, Long> {
    // 방문자가 주인의 어느 공간까지 볼 수 있는지 권한 조회용
    Optional<Relationship> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
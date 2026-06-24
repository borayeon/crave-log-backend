package com.cravelog.domain.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 특정 유저의 모든 카테고리를 가져옵니다.
    // 엔티티 설계 시 @OneToMany(mappedBy = "category")로 묶어두었기 때문에,
    // 카테고리만 조회해도 그 안의 Tag 목록을 함께 가져올 수 있습니다.
    List<Category> findAllByUserId(Long userId);
}
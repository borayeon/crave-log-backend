package com.cravelog.api.archive;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

enum CategoryType {
    WEBTOON, MOVIE, BOOK, GAME
}

enum ArchiveStatus {
    READING,    // 정주행중
    FINISHED,   // 최신화완료
    COMPLETED,  // 완결
    HIATUS,     // 휴재
    PLANNED     // 읽을예정
}

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String oauthId;

    private String email;

    @Column(nullable = false)
    private String nickname;

    private String profileImageUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Entity
@Table(name = "content_archives")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentArchive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false)
    private CategoryType categoryType;

    @Column(nullable = false)
    private String title;

    private String author; // 작가/감독/저자

    private String platform; // 네이버, 카카오 등

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(precision = 2, scale = 1)
    private Double rating; // 평점 (0.0 ~ 5.0)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArchiveStatus status;

    @Column(name = "review_note", columnDefinition = "TEXT")
    private String reviewNote;

    @Column(name = "theme_color")
    private String themeColor; // 테마 클래스 혹은 HEX

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

@Repository
interface ContentArchiveRepository extends JpaRepository<ContentArchive, Long> {

    // 특정 사용자의 카테고리별 통계
    @Query("SELECT a.categoryType, COUNT(a) FROM ContentArchive a WHERE a.user.id = :userId GROUP BY a.categoryType")
    List<Object[]> countByCategoryRaw(@Param("userId") Long userId);

    // 사용자별/카테고리별/상태별 필터링 조회
    @Query("SELECT a FROM ContentArchive a WHERE a.user.id = :userId " +
            "AND (:category IS NULL OR a.categoryType = :category) " +
            "AND (:status IS NULL OR a.status = :status) " +
            "ORDER BY a.updatedAt DESC")
    List<ContentArchive> findWithFilters(@Param("userId") Long userId,
                                         @Param("category") CategoryType category,
                                         @Param("status") ArchiveStatus status);
}

@Service
@RequiredArgsConstructor
class ArchiveService {
    private final ContentArchiveRepository repository;

    // 대시보드용 통계 데이터
    public Map<String, Long> getDashboardStats(Long userId) {
        return repository.countByCategoryRaw(userId).stream()
                .collect(Collectors.toMap(
                        obj -> ((CategoryType) obj[0]).name(),
                        obj -> (Long) obj[1]
                ));
    }

    // 필터링된 목록 조회
    public List<ContentArchive> getArchives(Long userId, CategoryType category, ArchiveStatus status) {
        return repository.findWithFilters(userId, category, status);
    }

    // 새로운 기록 저장
    public ContentArchive createArchive(ContentArchive archive) {
        return repository.save(archive);
    }
}

@RestController
@RequestMapping("/api/v1/archives")
@RequiredArgsConstructor
class ArchiveController {
    private final ArchiveService archiveService;

    // 현재는 테스트용으로 userId=1 고정 (추후 SecurityContext에서 추출)
    private final Long TEST_USER_ID = 1L;

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        return archiveService.getDashboardStats(TEST_USER_ID);
    }

    @GetMapping
    public List<ContentArchive> list(
            @RequestParam(required = false) CategoryType category,
            @RequestParam(required = false) ArchiveStatus status) {
        return archiveService.getArchives(TEST_USER_ID, category, status);
    }

    @PostMapping
    public ContentArchive create(@RequestBody ContentArchive archive) {
        // 실제 운영 시에는 인증 정보에서 User를 찾아 매핑
        return archiveService.createArchive(archive);
    }
}
package com.cravelog.domain.user;

import com.cravelog.domain.user.dto.VisitStatDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitService {

    private final DailyVisitRepository dailyVisitRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter LABEL_FORMATTER = DateTimeFormatter.ofPattern("MM/dd");

    /**
     * 1. 프로필 방문 기록 (중복 방지: 동일 IP/식별자는 당일 1회만 카운트)
     */
    @Transactional
    public void recordVisit(User targetUser, String visitorIp, Long authenticatedUserId) {
        if (targetUser == null) return;

        // 본인이 본인 프로필을 조회하는 경우는 카운트에서 제외
        if (authenticatedUserId != null && authenticatedUserId.equals(targetUser.getId())) {
            return;
        }

        LocalDate today = LocalDate.now();
        String identifier = (visitorIp != null && !visitorIp.isBlank()) ? visitorIp : "unknown";

        boolean isNewVisit = true;

        // Redis 기반 24시간 중복 방문 방지 (Key: Visit:Dedup:{userId}:{yyyy-MM-dd}:{identifier})
        if (redisTemplate != null) {
            try {
                String redisKey = String.format("Visit:Dedup:%d:%s:%s", targetUser.getId(), today, identifier);
                Boolean setResult = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", Duration.ofHours(24));
                isNewVisit = Boolean.TRUE.equals(setResult);
            } catch (Exception e) {
                log.warn("Redis 방문자 중복 체크 실패 (MySQL 직접 반영 진행): {}", e.getMessage());
                isNewVisit = true;
            }
        }

        if (isNewVisit) {
            // 일간 방문자 엔티티 조회 또는 신규 생성 후 증가
            DailyVisit dailyVisit = dailyVisitRepository.findByUserIdAndVisitDate(targetUser.getId(), today)
                    .orElseGet(() -> new DailyVisit(targetUser, today, 0L));

            dailyVisit.increment();
            dailyVisitRepository.save(dailyVisit);

            // 유저 전체 누적 방문자 수 증가
            targetUser.incrementTotalVisits();
            userRepository.save(targetUser);

            log.info("방문 기록 완료: 유저={}, IP={}, 오늘방문={}, 총방문={}", 
                    targetUser.getHandle(), identifier, dailyVisit.getCount(), targetUser.getTotalVisits());
        }
    }

    /**
     * 2. 오늘 방문자 수 조회
     */
    @Transactional(readOnly = true)
    public Long getTodayVisits(Long userId) {
        return dailyVisitRepository.findByUserIdAndVisitDate(userId, LocalDate.now())
                .map(DailyVisit::getCount)
                .orElse(0L);
    }

    /**
     * 3. 상세 방문자 통계 데이터 조회 (최근 7일 추이 포함)
     */
    @Transactional(readOnly = true)
    public VisitStatDto.Response getVisitStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate startDate = today.minusDays(6); // 최근 7일 (오늘 포함)

        Long todayVisits = getTodayVisits(userId);
        Long yesterdayVisits = dailyVisitRepository.findByUserIdAndVisitDate(userId, yesterday)
                .map(DailyVisit::getCount)
                .orElse(0L);

        List<DailyVisit> visitList = dailyVisitRepository.findRecentVisits(userId, startDate);
        Map<LocalDate, Long> visitMap = visitList.stream()
                .collect(Collectors.toMap(DailyVisit::getVisitDate, DailyVisit::getCount));

        List<VisitStatDto.DailyStat> recentDays = new ArrayList<>();
        long highest = 0L;

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long count = visitMap.getOrDefault(date, 0L);
            if (count > highest) {
                highest = count;
            }

            recentDays.add(VisitStatDto.DailyStat.builder()
                    .date(date.format(DATE_FORMATTER))
                    .dayLabel(date.format(LABEL_FORMATTER))
                    .count(count)
                    .build());
        }

        return VisitStatDto.Response.builder()
                .todayVisits(todayVisits)
                .yesterdayVisits(yesterdayVisits)
                .totalVisits(user.getTotalVisits())
                .highestVisits(highest)
                .recentDays(recentDays)
                .build();
    }
}

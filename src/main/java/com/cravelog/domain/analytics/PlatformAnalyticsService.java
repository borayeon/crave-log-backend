package com.cravelog.domain.analytics;

import com.cravelog.domain.analytics.dto.*;
import com.cravelog.domain.record.RecordRepository;
import com.cravelog.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformAnalyticsService {

    private final PlatformDailyStatRepository dailyStatRepository;
    private final PlatformAccessLogRepository accessLogRepository;
    private final UserRepository userRepository;
    private final RecordRepository recordRepository;
    private final StringRedisTemplate redisTemplate;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter LABEL_FORMATTER = DateTimeFormatter.ofPattern("MM/dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

    /**
     * 플랫폼 전체 방문 및 페이지뷰 기록 (중복 방지: 동일 IP는 당일 1회만 UV 증가)
     */
    @Transactional
    public void recordPlatformVisit(String ip, String userAgent, String path, String referer) {
        recordPlatformVisit(ip, userAgent, path, referer, null, "PAGE_VIEW", null);
    }

    @Transactional
    public void recordPlatformVisit(String ip, String userAgent, String path, String referer, Integer durationSeconds, String eventType, String eventDetail) {
        LocalDate today = LocalDate.now();
        String clientIp = (ip != null && !ip.isBlank()) ? ip : "unknown";
        String cleanPath = (path != null && !path.isBlank()) ? path : "/";
        if (cleanPath.length() > 100) cleanPath = cleanPath.substring(0, 100);

        String deviceType = detectDevice(userAgent);
        String browser = detectBrowser(userAgent);
        String os = detectOs(userAgent);
        String maskedIp = maskIp(clientIp);

        String type = (eventType != null && !eventType.isBlank()) ? eventType : "PAGE_VIEW";

        boolean isNewUv = true;

        if (redisTemplate != null) {
            try {
                String redisKey = String.format("Platform:Dedup:%s:%s", today, clientIp);
                Boolean setResult = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", Duration.ofHours(24));
                isNewUv = Boolean.TRUE.equals(setResult);
            } catch (Exception e) {
                log.warn("Redis 플랫폼 방문자 중복 체크 실패 (MySQL 직접 반영): {}", e.getMessage());
                isNewUv = true;
            }
        }

        // 1. 일별 통계 엔티티 조회 또는 신규 생성 후 증가
        PlatformDailyStat stat = dailyStatRepository.findByStatDate(today)
                .orElseGet(() -> new PlatformDailyStat(today));

        if (isNewUv) {
            stat.incrementUniqueVisitor();
        }
        if ("PAGE_VIEW".equalsIgnoreCase(type)) {
            stat.incrementPageView();
            stat.incrementDevice(deviceType);
        }
        dailyStatRepository.save(stat);

        // 2. 최근 접속 로그 저장
        try {
            PlatformAccessLog accessLog = PlatformAccessLog.builder()
                    .ipMasked(maskedIp)
                    .path(cleanPath)
                    .deviceType(deviceType)
                    .browser(browser)
                    .os(os)
                    .referer(referer != null && referer.length() > 255 ? referer.substring(0, 255) : referer)
                    .durationSeconds(durationSeconds)
                    .eventType(type)
                    .eventDetail(eventDetail != null && eventDetail.length() > 255 ? eventDetail.substring(0, 255) : eventDetail)
                    .accessedAt(LocalDateTime.now())
                    .build();
            accessLogRepository.save(accessLog);
        } catch (Exception e) {
            log.warn("접속 로그 저장 실패: {}", e.getMessage());
        }
    }

    /**
     * 개발자 대시보드용 플랫폼 통계 오버뷰 반환
     */
    @Transactional(readOnly = true)
    public PlatformOverviewDto getOverview(int days) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate startDate = today.minusDays(days - 1);

        // 1. 오늘 / 어제 통계
        PlatformDailyStat todayStat = dailyStatRepository.findByStatDate(today).orElse(null);
        PlatformDailyStat yesterdayStat = dailyStatRepository.findByStatDate(yesterday).orElse(null);

        long todayUv = todayStat != null ? todayStat.getUniqueVisitors() : 0L;
        long yesterdayUv = yesterdayStat != null ? yesterdayStat.getUniqueVisitors() : 0L;
        long todayPv = todayStat != null ? todayStat.getPageViews() : 0L;
        long yesterdayPv = yesterdayStat != null ? yesterdayStat.getPageViews() : 0L;

        // 2. 전체 누적 수치
        long totalUv = dailyStatRepository.sumTotalUniqueVisitors();
        long totalPv = dailyStatRepository.sumTotalPageViews();
        long totalUsers = userRepository.count();
        long totalRecords = recordRepository.count();

        // 3. 일별 트래픽 추이 (누락된 날짜는 0으로 채움)
        List<PlatformDailyStat> statsInRange = dailyStatRepository.findByStatDateBetweenOrderByStatDateAsc(startDate, today);
        Map<LocalDate, PlatformDailyStat> statMap = statsInRange.stream()
                .collect(Collectors.toMap(PlatformDailyStat::getStatDate, s -> s));

        List<DailyTrafficDto> recentDailyStats = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            PlatformDailyStat s = statMap.get(date);
            long uv = s != null ? s.getUniqueVisitors() : 0L;
            long pv = s != null ? s.getPageViews() : 0L;
            long mobile = s != null ? s.getMobileCount() : 0L;
            long desktop = s != null ? s.getDesktopCount() : 0L;

            recentDailyStats.add(DailyTrafficDto.builder()
                    .date(date.format(DATE_FORMATTER))
                    .dayLabel(date.format(LABEL_FORMATTER))
                    .uv(uv)
                    .pv(pv)
                    .mobileCount(mobile)
                    .desktopCount(desktop)
                    .build());
        }

        // 4. 디바이스 점유율
        long totalMobile = dailyStatRepository.sumTotalMobileCount();
        long totalDesktop = dailyStatRepository.sumTotalDesktopCount();
        long totalTablet = dailyStatRepository.sumTotalTabletCount();
        long deviceTotal = totalMobile + totalDesktop + totalTablet;

        List<NameValueDto> deviceShare = new ArrayList<>();
        if (deviceTotal > 0) {
            deviceShare.add(new NameValueDto("데스크톱", totalDesktop, Math.round((double) totalDesktop / deviceTotal * 1000.0) / 10.0));
            deviceShare.add(new NameValueDto("모바일", totalMobile, Math.round((double) totalMobile / deviceTotal * 1000.0) / 10.0));
            if (totalTablet > 0) {
                deviceShare.add(new NameValueDto("태블릿", totalTablet, Math.round((double) totalTablet / deviceTotal * 1000.0) / 10.0));
            }
        } else {
            deviceShare.add(new NameValueDto("데스크톱", 0, 100.0));
        }

        // 5. 브라우저 점유율 (상위 5개)
        List<Object[]> rawBrowsers = accessLogRepository.findTopBrowsers(PageRequest.of(0, 5));
        long browserTotal = rawBrowsers.stream().mapToLong(row -> (Long) row[1]).sum();
        List<NameValueDto> topBrowsers = rawBrowsers.stream()
                .map(row -> {
                    String name = (String) row[0];
                    long count = (Long) row[1];
                    double pct = browserTotal > 0 ? Math.round((double) count / browserTotal * 1000.0) / 10.0 : 0.0;
                    return new NameValueDto(name != null ? name : "기타", count, pct);
                })
                .collect(Collectors.toList());

        // 6. OS 점유율 (상위 5개)
        List<Object[]> rawOs = accessLogRepository.findTopOs(PageRequest.of(0, 5));
        long osTotal = rawOs.stream().mapToLong(row -> (Long) row[1]).sum();
        List<NameValueDto> topOs = rawOs.stream()
                .map(row -> {
                    String name = (String) row[0];
                    long count = (Long) row[1];
                    double pct = osTotal > 0 ? Math.round((double) count / osTotal * 1000.0) / 10.0 : 0.0;
                    return new NameValueDto(name != null ? name : "기타", count, pct);
                })
                .collect(Collectors.toList());

        // 7. 인기 페이지 경로 (상위 5개, 표시 이름 기준으로 합산 집계)
        List<Object[]> rawPaths = accessLogRepository.findTopPaths(PageRequest.of(0, 20));
        Map<String, Long> aggregatedPaths = new LinkedHashMap<>();
        for (Object[] row : rawPaths) {
            String rawPath = (String) row[0];
            String displayName = formatPathName(rawPath);
            long count = (Long) row[1];
            aggregatedPaths.put(displayName, aggregatedPaths.getOrDefault(displayName, 0L) + count);
        }
        long pathTotal = aggregatedPaths.values().stream().mapToLong(Long::longValue).sum();
        List<NameValueDto> topPaths = aggregatedPaths.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> {
                    double pct = pathTotal > 0 ? Math.round((double) e.getValue() / pathTotal * 1000.0) / 10.0 : 0.0;
                    return new NameValueDto(e.getKey(), e.getValue(), pct);
                })
                .collect(Collectors.toList());

        // 8. 유입 경로 (Referrer / Traffic Source) 분석 (상위 5개)
        List<Object[]> rawReferrers = accessLogRepository.findTopReferrers(PageRequest.of(0, 30));
        Map<String, Long> aggregatedReferrers = new LinkedHashMap<>();
        for (Object[] row : rawReferrers) {
            String rawRef = (String) row[0];
            String displayRef = classifyReferrer(rawRef);
            long count = (Long) row[1];
            aggregatedReferrers.put(displayRef, aggregatedReferrers.getOrDefault(displayRef, 0L) + count);
        }
        long refTotal = aggregatedReferrers.values().stream().mapToLong(Long::longValue).sum();
        List<NameValueDto> topReferrers = aggregatedReferrers.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> {
                    double pct = refTotal > 0 ? Math.round((double) e.getValue() / refTotal * 1000.0) / 10.0 : 0.0;
                    return new NameValueDto(e.getKey(), e.getValue(), pct);
                })
                .collect(Collectors.toList());

        // 9. 최다 검색 키워드 (상위 5개)
        List<Object[]> rawSearch = accessLogRepository.findTopSearchKeywords(PageRequest.of(0, 5));
        long searchTotal = rawSearch.stream().mapToLong(r -> (Long) r[1]).sum();
        List<NameValueDto> topSearchKeywords = rawSearch.stream()
                .map(row -> {
                    String keyword = (String) row[0];
                    long count = (Long) row[1];
                    double pct = searchTotal > 0 ? Math.round((double) count / searchTotal * 1000.0) / 10.0 : 0.0;
                    return new NameValueDto(keyword, count, pct);
                })
                .collect(Collectors.toList());

        // 10. 최다 클릭 외부 링크 (상위 5개)
        List<Object[]> rawClicks = accessLogRepository.findTopOutboundClicks(PageRequest.of(0, 5));
        long clicksTotal = rawClicks.stream().mapToLong(r -> (Long) r[1]).sum();
        List<NameValueDto> topOutboundClicks = rawClicks.stream()
                .map(row -> {
                    String link = (String) row[0];
                    long count = (Long) row[1];
                    double pct = clicksTotal > 0 ? Math.round((double) count / clicksTotal * 1000.0) / 10.0 : 0.0;
                    return new NameValueDto(link, count, pct);
                })
                .collect(Collectors.toList());

        // 11. 평균 화면 체류 시간 (초 단위)
        Double avgDuration = accessLogRepository.findAverageDurationSeconds();
        double averageDurationSeconds = avgDuration != null ? Math.round(avgDuration * 10.0) / 10.0 : 0.0;

        // 12. 최근 실시간 접속 로그 (최근 30개)
        List<PlatformAccessLog> recentLogsList = accessLogRepository.findTop30ByOrderByAccessedAtDesc();
        List<AccessLogDto> recentLogs = recentLogsList.stream()
                .map(logItem -> AccessLogDto.builder()
                        .id(logItem.getId())
                        .ipMasked(logItem.getIpMasked())
                        .path(formatPathName(logItem.getPath()))
                        .deviceType(logItem.getDeviceType())
                        .browser(logItem.getBrowser())
                        .os(logItem.getOs())
                        .timeAgo(formatTimeAgo(logItem.getAccessedAt()))
                        .accessedAt(logItem.getAccessedAt() != null ? logItem.getAccessedAt().format(TIME_FORMATTER) : "")
                        .build())
                .collect(Collectors.toList());

        return PlatformOverviewDto.builder()
                .todayUv(todayUv)
                .yesterdayUv(yesterdayUv)
                .todayPv(todayPv)
                .yesterdayPv(yesterdayPv)
                .totalUv(totalUv)
                .totalPv(totalPv)
                .totalUsers(totalUsers)
                .totalRecords(totalRecords)
                .recentDailyStats(recentDailyStats)
                .deviceShare(deviceShare)
                .topBrowsers(topBrowsers)
                .topOs(topOs)
                .topPaths(topPaths)
                .topReferrers(topReferrers)
                .topSearchKeywords(topSearchKeywords)
                .topOutboundClicks(topOutboundClicks)
                .averageDurationSeconds(averageDurationSeconds)
                .recentLogs(recentLogs)
                .build();
    }

    /**
     * ⭐️ 매일 새벽 3시: 90일 지난 오래된 상세 접속 로그 자동 정리 (DB 용량 무한 증가 방지)
     * (일별 집계 테이블 PlatformDailyStat은 영구 보존되므로 통계 그래프는 손실되지 않음)
     */
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldAccessLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
        try {
            accessLogRepository.deleteByAccessedAtBefore(cutoff);
            log.info("🧹 [스케줄러] 90일 지난 오래된 상세 플랫폼 접속 로그 정리 완료 (기준: {})", cutoff);
        } catch (Exception e) {
            log.error("오래된 접속 로그 정리 중 오류 발생: {}", e.getMessage());
        }
    }

    // --- 유틸리티 메서드 ---

    public static String classifyReferrer(String ref) {
        if (ref == null || ref.isBlank()) return "직접 방문 (Direct / Bookmark)";
        String lower = ref.toLowerCase();
        if (lower.contains("naver.com")) return "네이버 (Naver)";
        if (lower.contains("google.com") || lower.contains("google.co.kr")) return "구글 (Google)";
        if (lower.contains("kakao.com") || lower.contains("kakaotalk")) return "카카오톡 (Kakao)";
        if (lower.contains("instagram.com")) return "인스타그램 (Instagram)";
        if (lower.contains("facebook.com")) return "페이스북 (Facebook)";
        if (lower.contains("youtube.com") || lower.contains("youtu.be")) return "유튜브 (YouTube)";
        if (lower.contains("twitter.com") || lower.contains("t.co") || lower.contains("x.com")) return "X / Twitter";
        if (lower.contains("cravelog.me") || lower.contains("localhost")) return "내부 이동 (Internal)";
        try {
            java.net.URI uri = java.net.URI.create(ref);
            String host = uri.getHost();
            return host != null ? host : "기타 웹사이트";
        } catch (Exception e) {
            return "기타 웹사이트";
        }
    }

    public static String maskIp(String ip) {
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) return "unknown";
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "localhost".equalsIgnoreCase(ip)) {
            return "127.0.***.*** (Local)";
        }
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".***.***";
        }
        if (ip.contains(":")) {
            String[] v6 = ip.split(":");
            if (v6.length >= 2) {
                return v6[0] + ":" + v6[1] + ":****:****";
            }
        }
        return ip.replaceAll("(\\d{1,3}\\.\\d{1,3})\\..*", "$1.***.***");
    }

    public static String detectDevice(String ua) {
        if (ua == null) return "DESKTOP";
        String lower = ua.toLowerCase();
        if (lower.contains("ipad") || lower.contains("tablet")) return "TABLET";
        if (lower.contains("mobile") || lower.contains("android") || lower.contains("iphone") || lower.contains("ipod")) return "MOBILE";
        return "DESKTOP";
    }

    public static String detectBrowser(String ua) {
        if (ua == null) return "Chrome";
        if (ua.contains("Edg/")) return "Edge";
        if (ua.contains("Whale/")) return "Naver Whale";
        if (ua.contains("OPR/") || ua.contains("Opera")) return "Opera";
        if (ua.contains("Chrome/") || ua.contains("CriOS/")) return "Chrome";
        if (ua.contains("Safari/") && !ua.contains("Chrome")) return "Safari";
        if (ua.contains("Firefox/")) return "Firefox";
        return "Chrome";
    }

    public static String detectOs(String ua) {
        if (ua == null) return "Windows";
        if (ua.contains("Windows NT 10") || ua.contains("Windows NT 11")) return "Windows";
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("iPhone") || ua.contains("iPad") || ua.contains("iPod")) return "iOS";
        if (ua.contains("Mac OS X") || ua.contains("Macintosh")) return "macOS";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("Linux")) return "Linux";
        return "Other";
    }

    public static String formatPathName(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) return "홈 / 메인";
        if (path.contains("profile")) return "프로필 뷰 (Profile)";
        if (path.contains("timeline")) return "타임라인 뷰 (Timeline)";
        if (path.contains("archive")) return "아카이브 뷰 (Archive)";
        if (path.contains("search")) return "검색 페이지 (Search)";
        if (path.contains("settings") || path.contains("account")) return "계정 설정 (Settings)";
        if (path.contains("analytics")) return "분석 대시보드 (Analytics)";
        return path;
    }

    public static String formatTimeAgo(LocalDateTime time) {
        if (time == null) return "";
        Duration duration = Duration.between(time, LocalDateTime.now());
        long seconds = Math.abs(duration.getSeconds());
        if (seconds < 60) return "방금 전";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "분 전";
        long hours = minutes / 60;
        if (hours < 24) return hours + "시간 전";
        long days = hours / 24;
        return days + "일 전";
    }
}

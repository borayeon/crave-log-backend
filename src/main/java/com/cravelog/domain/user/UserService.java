package com.cravelog.domain.user;

import com.cravelog.domain.user.dto.ProfileDto;
import com.cravelog.domain.user.dto.VisitStatDto;
import com.cravelog.domain.record.RecordRepository;
import com.cravelog.domain.record.Record;
import com.cravelog.domain.tag.CategoryRepository;
import com.cravelog.domain.tag.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RecordRepository recordRepository;
    private final CategoryRepository categoryRepository;
    private final VisitService visitService;
    private final FollowRepository followRepository;
    private final DailyVisitRepository dailyVisitRepository;

    private static final java.util.regex.Pattern HANDLE_PATTERN = java.util.regex.Pattern.compile("^[a-z0-9._-]{2,30}$");
    private static final java.util.regex.Pattern PASSWORD_PATTERN = java.util.regex.Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$");

    private static final int MAX_PIN_ATTEMPTS = 5;
    private static final long PIN_LOCKOUT_MILLIS = 5 * 60 * 1000L;
    private final java.util.concurrent.ConcurrentHashMap<String, PinAttempt> pinAttempts = new java.util.concurrent.ConcurrentHashMap<>();

    private record PinAttempt(int count, long lockedUntil) {}

    /**
     * 외부 공유용 (게스트) 프로필 조회 기능 (방문자 수 추적 및 팔로워 여부 판별 포함)
     */
    @Transactional
    public ProfileDto.Response getPublicProfile(String handle, String personaId, String visitorIp, Long authenticatedUserId) {
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // ⭐️ 관리자(ROLE_ADMIN) 계정은 퍼블릭 프로필 조회 불가 (존재 여부 은닉)
        if (user.getUserRole() == UserRole.ROLE_ADMIN) {
            throw new IllegalArgumentException("User not found");
        }

        // ⭐️ 방문자 수 추적 기록 (중복 방지 & 본인 조회 제외)
        visitService.recordVisit(user, visitorIp, authenticatedUserId);

        Long todayVisits = visitService.getTodayVisits(user.getId());

        boolean isOwner = authenticatedUserId != null && authenticatedUserId.equals(user.getId());
        boolean isFollower = isOwner || (authenticatedUserId != null
                && followRepository.existsByFollowerIdAndFollowingId(authenticatedUserId, user.getId()));

        // 알아서 필터링된 안전한 Response가 만들어집니다.
        return ProfileDto.Response.from(user, isOwner, personaId, todayVisits, isFollower, java.util.Set.of());
    }

    @Transactional
    public ProfileDto.Response getPublicProfile(String handle, String personaId) {
        return getPublicProfile(handle, personaId, null, null);
    }

    /**
     * ⭐️ 프로필 서브 탭 / 페르소나 PIN 서버 사이드 검증 및 잠금 해제된 프로필 반환
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public ProfileDto.Response verifyProfilePin(
            String handle,
            ProfileDto.VerifyPinRequest request,
            Long authenticatedUserId,
            String clientIp
    ) {
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getUserRole() == UserRole.ROLE_ADMIN) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        if (request == null || request.getTargetType() == null || request.getTargetId() == null || request.getPasscode() == null) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        String actorKey = (authenticatedUserId != null ? "u:" + authenticatedUserId : "ip:" + (clientIp != null ? clientIp : "unknown"))
                + ":h:" + user.getId() + ":" + request.getTargetType() + ":" + request.getTargetId();
        long now = System.currentTimeMillis();
        PinAttempt attempt = pinAttempts.get(actorKey);
        if (attempt != null && attempt.lockedUntil() > now) {
            throw new IllegalArgumentException("PIN 입력 시도가 너무 많습니다. 5분 후 다시 시도해 주세요.");
        }

        Map<String, Object> idolData = user.getIdolData();
        String expectedPin = null;
        String unlockedKey = null;

        if ("tab".equalsIgnoreCase(request.getTargetType())) {
            if (idolData != null && idolData.get("tabPasscodes") instanceof Map<?, ?> tabPasscodes) {
                Object pinObj = tabPasscodes.get(request.getTargetId());
                if (pinObj != null) {
                    expectedPin = String.valueOf(pinObj).trim();
                    unlockedKey = "tab:" + request.getTargetId();
                }
            }
        } else if ("persona".equalsIgnoreCase(request.getTargetType())) {
            if (idolData != null && idolData.get("personas") instanceof Map<?, ?> personas) {
                for (Map.Entry<?, ?> entry : personas.entrySet()) {
                    String personaKey = String.valueOf(entry.getKey());
                    if (!(entry.getValue() instanceof Map<?, ?> persona)) continue;
                    String personaToken = persona.get("token") != null ? String.valueOf(persona.get("token")) : null;
                    if (request.getTargetId().equals(personaKey) || request.getTargetId().equals(personaToken)) {
                        Object pinObj = persona.get("passcode");
                        if (pinObj != null) {
                            expectedPin = String.valueOf(pinObj).trim();
                            unlockedKey = "persona:" + personaKey;
                        }
                        break;
                    }
                }
            }
        }

        if (expectedPin == null || expectedPin.isEmpty() || !expectedPin.equals(request.getPasscode().trim())) {
            int nextCount = (attempt != null && attempt.lockedUntil() <= now) ? attempt.count() + 1 : 1;
            long lockedUntil = (nextCount >= MAX_PIN_ATTEMPTS) ? now + PIN_LOCKOUT_MILLIS : 0L;
            pinAttempts.put(actorKey, new PinAttempt(nextCount, lockedUntil));
            throw new IllegalArgumentException("비밀번호(PIN)가 일치하지 않습니다.");
        }

        pinAttempts.remove(actorKey);

        // 기존 세션에서 해제된 타겟들도 서버에서 유효성을 재검증하거나 이번 해제 키를 포함
        java.util.Set<String> validUnlocked = new java.util.HashSet<>();
        if (request.getUnlockedTargets() != null) {
            validUnlocked.addAll(request.getUnlockedTargets());
        }
        validUnlocked.add(unlockedKey);

        Long todayVisits = visitService.getTodayVisits(user.getId());
        boolean isOwner = authenticatedUserId != null && authenticatedUserId.equals(user.getId());
        boolean isFollower = isOwner || (authenticatedUserId != null
                && followRepository.existsByFollowerIdAndFollowingId(authenticatedUserId, user.getId()));

        return ProfileDto.Response.from(user, isOwner, request.getPersonaId(), todayVisits, isFollower, validUnlocked);
    }

    /**
     * 내 프로필 조회 기능 (마이페이지용)
     */
    @Transactional(readOnly = true)
    public ProfileDto.Response getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Long todayVisits = visitService.getTodayVisits(userId);
        return ProfileDto.Response.from(user, true, null, todayVisits);
    }

    /**
     * ⭐️ 방문자 통계 조회 (상세 대시보드용 - 관리자 계정 은닉 포함)
     */
    @Transactional(readOnly = true)
    public VisitStatDto.Response getVisitStatsByHandle(String handle) {
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getUserRole() == UserRole.ROLE_ADMIN) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        return visitService.getVisitStats(user.getId());
    }

    /**
     * 내 프로필 업데이트
     */
    @Transactional
    public void updateProfile(Long userId, ProfileDto.UpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (request.getHandle() != null && !request.getHandle().trim().isEmpty()) {
            String newHandle = request.getHandle().trim().toLowerCase();

            if (!newHandle.equals(user.getHandle())) {
                if (!HANDLE_PATTERN.matcher(newHandle).matches() || "admin_craver".equals(newHandle)) {
                    throw new IllegalArgumentException("고유 아이디는 영문 소문자, 숫자, 마침표(.), 언더바(_), 하이픈(-) 조합으로 2~30자여야 합니다.");
                }
                if (userRepository.existsByHandle(newHandle)) {
                    throw new IllegalArgumentException("이미 사용 중인 고유 아이디입니다.");
                }
                user.updateHandle(newHandle); // 아이디 교체
            }
        }

        user.updateProfile(
                request.getName(), request.getProfileImageUrl(), request.getRole(), request.getMajor(), request.getLocation(),
                request.getBio(), request.getStatus(), request.getTags(), request.getGoals(),
                request.getDeveloper(), request.getCareer(), request.getIdol(), request.getPrivacy(),
                request.getLinks()
        );

        userRepository.save(user);
    }

    /**
     * ⭐️ 유저 검색 기능 (검색 노출 여부, 항목별 검색 허용 설정, 다중 조건 및 경량 DTO 반영)
     */
    @Transactional(readOnly = true)
    public List<ProfileDto.SearchResponse> searchUsers(String keyword, String filter, Long currentUserId) {
        List<User> users = userRepository.findByUserRole(UserRole.ROLE_USER);

        // 팔로워 수 및 팔로우 여부 일괄 조회 (N+1 쿼리 방지)
        java.util.Map<Long, Long> followerCountMap = new java.util.HashMap<>();
        for (Object[] row : followRepository.countFollowersGroupByFollowingId()) {
            if (row[0] != null && row[1] != null) {
                followerCountMap.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
            }
        }
        java.util.Set<Long> followingIds = (currentUserId != null)
                ? followRepository.findFollowingIdsByFollowerId(currentUserId)
                : java.util.Set.of();

        String rawKeyword = (keyword == null) ? "" : keyword.trim();
        // @핸들 또는 #태그 형태로 검색했을 때 기호 제거 지원
        String normalizedKeyword = rawKeyword.replaceFirst("^[@#]+", "").trim().toLowerCase();
        String filterMode = (filter == null || filter.isBlank()) ? "all" : filter.trim().toLowerCase();

        // 검색어가 없으면 검색 허용(isSearchable)된 일반 유저 전체 반환
        if (normalizedKeyword.isEmpty()) {
            return users.stream()
                    .filter(user -> user.getUserRole() != UserRole.ROLE_ADMIN)
                    .filter(User::isSearchable)
                    .map(user -> ProfileDto.SearchResponse.from(
                            user,
                            followerCountMap.getOrDefault(user.getId(), 0L),
                            followingIds.contains(user.getId()),
                            List.of()
                    ))
                    .collect(Collectors.toList());
        }

        // 검색어가 있을 경우 항목별 공개 설정(isFieldSearchable) 및 필터 모드에 맞춰 매칭 및 정확도 정렬
        record MatchedUser(User user, List<String> matchedFields, int score) {}

        return users.stream()
                .filter(user -> user.getUserRole() != UserRole.ROLE_ADMIN)
                .filter(User::isSearchable)
                .map(user -> {
                    List<String> matched = new java.util.ArrayList<>();
                    int score = 0;

                    // 1. 이름 / 핸들 검색
                    if ("all".equals(filterMode) || "name".equals(filterMode)) {
                        boolean nameMatch = containsIgnoreCase(user.getName(), normalizedKeyword);
                        boolean handleMatch = containsIgnoreCase(user.getHandle(), normalizedKeyword);
                        if (nameMatch || handleMatch) {
                            matched.add("name");
                            if (normalizedKeyword.equalsIgnoreCase(user.getHandle()) || normalizedKeyword.equalsIgnoreCase(user.getName())) {
                                score += 100;
                            } else {
                                score += 50;
                            }
                        }
                    }

                    // 2. 직무 / 전공 / 위치 검색 (본인이 직무/소속 검색을 허용한 경우만)
                    if (("all".equals(filterMode) || "role".equals(filterMode)) && user.isFieldSearchable("role")) {
                        if (containsIgnoreCase(user.getRole(), normalizedKeyword)
                                || containsIgnoreCase(user.getMajor(), normalizedKeyword)
                                || containsIgnoreCase(user.getLocation(), normalizedKeyword)) {
                            matched.add("role");
                            score += 30;
                        }
                    }

                    // 3. 키워드 태그 검색 (본인이 태그 검색을 허용한 경우만)
                    if (("all".equals(filterMode) || "tag".equals(filterMode)) && user.isFieldSearchable("tags")) {
                        if (user.getTags() != null && user.getTags().stream().anyMatch(t -> containsIgnoreCase(t, normalizedKeyword))) {
                            matched.add("tags");
                            score += 40;
                        }
                    }

                    // 4. 한 줄 소개 / 상태 메시지 검색 (본인이 소개글 검색을 허용한 경우만)
                    if (("all".equals(filterMode) || "bio".equals(filterMode)) && user.isFieldSearchable("bio")) {
                        if (containsIgnoreCase(user.getBio(), normalizedKeyword)
                                || containsIgnoreCase(user.getStatusMessage(), normalizedKeyword)) {
                            matched.add("bio");
                            score += 20;
                        }
                    }

                    return new MatchedUser(user, matched, score);
                })
                .filter(mu -> !mu.matchedFields().isEmpty())
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.score(), a.score());
                    if (cmp != 0) return cmp;
                    return Long.compare(b.user().getTotalVisits(), a.user().getTotalVisits());
                })
                .map(mu -> ProfileDto.SearchResponse.from(
                        mu.user(),
                        followerCountMap.getOrDefault(mu.user().getId(), 0L),
                        followingIds.contains(mu.user().getId()),
                        mu.matchedFields()
                ))
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String source, String targetLower) {
        return source != null && !source.isBlank() && source.toLowerCase().contains(targetLower);
    }

    /**
     * ⭐️ 내 검색 노출 설정 단독 업데이트
     */
    @Transactional
    public void updateSearchPrivacy(Long userId, ProfileDto.SearchPrivacyRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.updateSearchPrivacy(
                request.getIsSearchable(),
                request.getSearchRole(),
                request.getSearchBio(),
                request.getSearchTags()
        );
        userRepository.save(user);
    }

    /**
     * ⭐️ 비밀번호 변경
     */
    @Transactional
    public void changePassword(Long userId, ProfileDto.ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // ⭐️ 카카오 등 소셜 유저는 변경 불가 방어
        if (user.getOauthProvider() != null) {
            throw new IllegalArgumentException("소셜 연동 계정은 비밀번호를 변경할 수 없습니다.");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (request.getNewPassword() == null || !PASSWORD_PATTERN.matcher(request.getNewPassword()).matches()) {
            throw new IllegalArgumentException("새 비밀번호는 영문, 숫자, 특수문자를 포함한 8~64자여야 합니다.");
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    /**
     * ⭐️ 계정 완전 탈퇴
     */
    @Transactional
    public void deleteAccount(Long userId, ProfileDto.DeleteAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // ⭐️ 소셜 연동 계정이 아닐 때(이메일 가입자일 때)만 비밀번호 검증
        if (user.getOauthProvider() == null) {
            if (request == null || request.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
        }

        // 1. 유저의 모든 팔로우 관계 삭제 (팔로워 및 팔로잉 외래키 무결성 보장)
        followRepository.deleteAllByUserId(userId);

        // 2. 유저의 일별 방문자 통계 기록 삭제
        dailyVisitRepository.deleteAllByUserId(userId);

        // 3. 유저가 작성한 모든 기록 지우기 (RecordTag는 CascadeType.ALL에 의해 연쇄 삭제)
        List<Record> records = recordRepository.findAllByUserIdWithTags(userId);
        recordRepository.deleteAll(records);

        // 4. 유저가 작성한 모든 카테고리(및 하위 태그들) 지우기 (Tag는 CascadeType.ALL에 의해 연쇄 삭제)
        List<Category> categories = categoryRepository.findAllByUserId(userId);
        categoryRepository.deleteAll(categories);

        // 5. 마지막으로 유저 본인 지우기
        userRepository.delete(user);
    }
}
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RecordRepository recordRepository;
    private final CategoryRepository categoryRepository;
    private final VisitService visitService;

    /**
     * 외부 공유용 (게스트) 프로필 조회 기능 (방문자 수 추적 포함)
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

        // 알아서 필터링된 안전한 Response가 만들어집니다.
        return ProfileDto.Response.from(user, false, personaId, todayVisits);
    }

    @Transactional
    public ProfileDto.Response getPublicProfile(String handle, String personaId) {
        return getPublicProfile(handle, personaId, null, null);
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
     * ⭐️ 방문자 통계 조회 (상세 대시보드용)
     */
    @Transactional(readOnly = true)
    public VisitStatDto.Response getVisitStatsByHandle(String handle) {
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
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
     * ⭐️ 유저 검색 기능 (안전하게 DTO로 변환하여 반환)
     */
    @Transactional(readOnly = true)
    public List<ProfileDto.Response> searchUsers(String keyword) {
        List<User> users;

        // 검색어가 없으면 일반 유저(ROLE_USER) 목록만 조회 (관리자 계정 원천 제외)
        if (keyword == null || keyword.trim().isEmpty()) {
            users = userRepository.findByUserRole(UserRole.ROLE_USER);
        } else {
            // ⭐️ ROLE_ADMIN을 제외하는 커스텀 쿼리 호출
            users = userRepository.searchUsers(keyword);
        }

        return users.stream()
                .filter(user -> user.getUserRole() != UserRole.ROLE_ADMIN) // 이중 방어 필터링
                .map(user -> ProfileDto.Response.from(user, false)) // 퍼블릭 프로필 정보로 변환하여 반환
                .collect(Collectors.toList());
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
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
        }

        // 1. 유저가 작성한 모든 기록 지우기
        List<Record> records = recordRepository.findAllByUserIdWithTags(userId);
        recordRepository.deleteAll(records);

        // 2. 유저가 작성한 모든 카테고리(및 하위 태그들) 지우기
        List<Category> categories = categoryRepository.findAllByUserId(userId);
        categoryRepository.deleteAll(categories);

        // 3. 마지막으로 유저 본인 지우기
        userRepository.delete(user);
    }
}
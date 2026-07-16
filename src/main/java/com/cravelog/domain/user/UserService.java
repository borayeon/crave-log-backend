package com.cravelog.domain.user;

import com.cravelog.domain.user.dto.ProfileDto;
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
    private final PasswordEncoder passwordEncoder; // ⭐️ 비밀번호 검증용
    private final RecordRepository recordRepository; // ⭐️ 탈퇴 시 기록 삭제용
    private final CategoryRepository categoryRepository; // ⭐️ 탈퇴 시 폴더 삭제용

    /**
     * 외부 공유용 (게스트) 프로필 조회 기능
     */
    @Transactional(readOnly = true)
    public ProfileDto.Response getPublicProfile(String handle) {
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필입니다."));

        return ProfileDto.Response.from(user, false);
    }

    /**
     * 내 프로필 조회 기능 (마이페이지용)
     */
    @Transactional(readOnly = true)
    public ProfileDto.Response getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return ProfileDto.Response.from(user, true);
    }

    /**
     * 내 프로필 업데이트
     */
    @Transactional
    public void updateProfile(Long userId, ProfileDto.UpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // ⭐️ 1. 아이디 중복 검사 및 교체 (소문자 변환 및 공백 제거로 더 안전하게 검증)
        if (request.getHandle() != null && !request.getHandle().trim().isEmpty()) {
            String newHandle = request.getHandle().trim().toLowerCase();

            if (!newHandle.equals(user.getHandle())) { // 기존 아이디와 다를 때만 검사
                if (userRepository.existsByHandle(newHandle)) {
                    // ⭐️ 이제 이 에러가 무시되지 않고 프론트엔드에 토스트 알림으로 뜹니다!
                    throw new IllegalArgumentException("이미 사용 중인 고유 아이디입니다.");
                }
                user.updateHandle(newHandle); // 아이디 교체
            }
        }

        // ⭐️ 2. 기존 프로필 정보 업데이트
        user.updateProfile(
                request.getName(), request.getProfileImageUrl(), request.getRole(), request.getMajor(), request.getLocation(),
                request.getBio(), request.getStatus(), request.getTags(), request.getGoals(),
                request.getDeveloper(), request.getCareer(), request.getIdol(), request.getPrivacy()
        );

        // ⭐️ 3. 핵심: 자동 저장에만 의존하지 않고 명시적으로 강제 DB 저장!!
        userRepository.save(user);
    }

    /**
     * ⭐️ 유저 검색 기능 (안전하게 DTO로 변환하여 반환)
     */
    @Transactional(readOnly = true)
    public List<ProfileDto.Response> searchUsers(String keyword) {
        List<User> users;

        // 검색어가 없으면 전체를 가져오도록 처리
        if (keyword == null || keyword.trim().isEmpty()) {
            users = userRepository.findAll();
        } else {
            // ⭐️ 기존에 구현해둔 안전한 커스텀 쿼리 메서드 호출 (MySQL 특수문자 이스케이프 버그 방어)
            users = userRepository.searchUsers(keyword);
        }

        return users.stream()
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
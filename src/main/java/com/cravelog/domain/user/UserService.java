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

        // ⭐️ 프로필 이미지 URL 포함 업데이트
        user.updateProfile(
                request.getName(), request.getProfileImageUrl(), request.getRole(), request.getMajor(), request.getLocation(),
                request.getBio(), request.getStatus(), request.getTags(), request.getGoals(),
                request.getDeveloper(), request.getCareer(), request.getIdol(), request.getPrivacy()
        );
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

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
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
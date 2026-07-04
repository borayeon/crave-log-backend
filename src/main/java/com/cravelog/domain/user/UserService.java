package com.cravelog.domain.user;

import com.cravelog.domain.user.dto.ProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 외부 공유용 (게스트) 프로필 조회 기능
     * @param handle 유저 고유 핸들러 (예: taekyeong.dev)
     */
    @Transactional(readOnly = true)
    public ProfileDto.Response getPublicProfile(String handle) {
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필입니다."));

        // isOwner = false 로 넘겨서 비공개 데이터는 null 처리하여 반환합니다.
        return ProfileDto.Response.from(user, false);
    }

    /**
     * 내 프로필 조회 기능 (마이페이지용)
     * @param userId 로그인한 사용자의 PK
     */
    @Transactional(readOnly = true)
    public ProfileDto.Response getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // isOwner = true 로 넘겨서 숨겨진 데이터까지 모두 반환합니다.
        return ProfileDto.Response.from(user, true);
    }

    /**
     * 내 프로필 업데이트
     */
    @Transactional
    public void updateProfile(Long userId, ProfileDto.UpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // User 엔티티의 정보 수정 (profileImageUrl 추가)
        user.updateProfile(
                request.getName(), request.getProfileImageUrl(), request.getRole(), request.getMajor(), request.getLocation(),
                request.getBio(), request.getStatus(), request.getTags(), request.getGoals(),
                request.getDeveloper(), request.getCareer(), request.getIdol(), request.getPrivacy()
        );
        // ⭐️ 검색 기능 추가
        @Transactional(readOnly = true)
        public List<ProfileDto.Response> searchUsers(String keyword) {
            return userRepository.findByNameContainingIgnoreCaseOrHandleContainingIgnoreCase(keyword, keyword)
                    .stream()
                    .map(user -> ProfileDto.Response.from(user, false)) // 퍼블릭 프로필 정보로 변환
                    .collect(Collectors.toList());
        }
    }
package com.cravelog.domain.record;

import com.cravelog.domain.record.dto.RecordDto;
import com.cravelog.domain.tag.CategoryRepository;
import com.cravelog.domain.tag.dto.TagTreeDto;
import com.cravelog.domain.user.User;
import com.cravelog.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordRepository recordRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * 태그 트리(폴더 구조) 조회
     */
    @Transactional(readOnly = true)
    public List<TagTreeDto.CategoryResponse> getTagTree(String handle) {
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        return categoryRepository.findAllByUserId(user.getId()).stream()
                .map(TagTreeDto.CategoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 기록(타임라인/아카이브) 목록 조회
     */
    @Transactional(readOnly = true)
    public List<RecordDto.Response> getRecords(String handle, boolean isOwner) {
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        List<Record> records = recordRepository.findAllByUserIdWithTags(user.getId());

        return records.stream()
                // 본인이 아닐 경우(Guest) isPublic == true 인 기록만 필터링 🔒
                .filter(record -> isOwner || record.isPublic())
                .map(RecordDto.Response::from)
                .collect(Collectors.toList());
    }
}
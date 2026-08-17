package com.cravelog.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    @Value("${file.upload-dir:/app/uploads/}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "파일이 없습니다."));
        }

        try {
            // 1. 업로드 폴더가 없으면 자동 생성
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 2. 파일명 중복 방지를 위한 UUID 생성 (예: 123e4567_myprofile.jpg)
            String originalFilename = file.getOriginalFilename();
            // 띄어쓰기 등 파일명 오류 방지를 위해 원본 이름도 정제하면 좋습니다.
            String safeFilename = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
            String fileName = UUID.randomUUID().toString() + "_" + safeFilename;

            // 3. 물리적 파일 저장 (마운트된 폴더로 쏙!)
            File dest = new File(uploadDir + fileName);
            file.transferTo(dest);

            // 4. 클라이언트가 이미지를 볼 수 있는 URL 반환 (프론트가 이걸 DB에 저장합니다)
            String imageUrl = "/uploads/" + fileName;

            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            log.error("파일 업로드 실패", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 저장 실패"));
        }
    }
}
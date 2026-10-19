package com.cravelog.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    @Value("${file.upload-dir:/app/uploads/}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "파일이 없습니다."));
        }

        // 1. MIME Content-Type 화이트리스트 검증 (SVG, HTML 등 스크립트 실행 가능 포맷 원천 차단)
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            log.warn("🚨 허용되지 않은 파일 MIME 타입 업로드 차단: contentType={}", contentType);
            return ResponseEntity.badRequest().body(Map.of("message", "JPG, PNG, GIF, WEBP 형식의 이미지 파일만 업로드할 수 있습니다."));
        }

        // 2. 파일 확장자 화이트리스트 및 Null 안전성 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank() || !originalFilename.contains(".")) {
            return ResponseEntity.badRequest().body(Map.of("message", "유효하지 않은 파일 이름입니다."));
        }

        String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            log.warn("🚨 허용되지 않은 파일 확장자 업로드 차단: ext={}", ext);
            return ResponseEntity.badRequest().body(Map.of("message", "허용되지 않는 파일 확장자입니다."));
        }

        try {
            // 3. 업로드 폴더가 없으면 자동 생성
            File directory = new File(uploadDir).getCanonicalFile();
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 4. 안전한 UUID + 정규화된 확장자로만 파일명 생성
            String fileName = UUID.randomUUID().toString() + "." + ext;
            File dest = new File(directory, fileName).getCanonicalFile();

            // 5. Path Traversal(경로 조작) 방어 검증
            if (!dest.getPath().startsWith(directory.getPath() + File.separator)) {
                log.error("🚨 Path Traversal 시도 차단: {}", dest.getPath());
                return ResponseEntity.badRequest().body(Map.of("message", "잘못된 파일 경로입니다."));
            }

            file.transferTo(dest);

            String imageUrl = "/uploads/" + fileName;
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            log.error("파일 업로드 실패", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "서버 저장 실패"));
        }
    }
}
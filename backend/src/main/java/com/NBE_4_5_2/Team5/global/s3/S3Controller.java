package com.NBE_4_5_2.Team5.global.s3;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class S3Controller {

    private final S3UploadService s3UploadService;

    public S3Controller(S3UploadService s3UploadService) {
        this.s3UploadService = s3UploadService;
    }

    @PostMapping("/api/uploadFile")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = s3UploadService.saveFile(file);
            return ResponseEntity.ok(fileUrl); // 성공적으로 업로드된 파일의 URL 반환
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("파일 업로드 실패: " + e.getMessage());
        }
    }
}
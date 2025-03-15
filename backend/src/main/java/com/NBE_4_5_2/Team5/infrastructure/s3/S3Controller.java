package com.NBE_4_5_2.Team5.infrastructure.s3;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "S3 API", description = "S3에 데이터를 저장하는 API")
@RestController
public class S3Controller {

	private final S3UploadService s3UploadService;

	public S3Controller(S3UploadService s3UploadService) {
		this.s3UploadService = s3UploadService;
	}

	@Operation(summary = "파일 업로드", description = "새로운 파일을 업로드합니다.")
	@SecurityRequirement(name = "cookieAuth")
	@PreAuthorize("isAuthenticated")
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
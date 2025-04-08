package com.NBE_4_5_2.Team5.infrastructure.s3

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

@Tag(name = "S3 API", description = "S3에 데이터를 저장하는 API")
@RestController
@RequestMapping("/api")
class S3Controller(
    private val s3UploadService: S3UploadService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Operation(summary = "파일 업로드", description = "새로운 파일을 업로드합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated")
    @PostMapping(
        path = ["/uploadFile"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun uploadFile(@RequestParam file: MultipartFile): ResponseEntity<String> =
        try {
            val fileUrl = s3UploadService.saveFile(file)
            ResponseEntity.ok(fileUrl)
        } catch (e: IOException) {
            log.error("파일 업로드 실패", e)
            ResponseEntity
                .status(500)
                .body("파일 업로드 실패: ${e.message.orEmpty()}")
        }
}


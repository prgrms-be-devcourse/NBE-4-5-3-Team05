package com.NBE_4_5_2.Team5.infrastructure.s3

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class S3UploadService(
    private val amazonS3: AmazonS3,
    @Value("\${cloud.aws.s3.bucket}")
    private val bucket: String
) {

    fun saveFile(multipartFile: MultipartFile): String {
        // 원본 파일명이 없으면 예외 처리
        val key = multipartFile.originalFilename
            ?: throw IllegalArgumentException("업로드할 파일 이름이 없습니다.")

        // 메타데이터 설정
        val metadata = ObjectMetadata().apply {
            contentLength = multipartFile.size
            contentType = multipartFile.contentType
        }

        // InputStream을 안전하게 사용하고 닫기
        multipartFile.inputStream.use { input ->
            amazonS3.putObject(bucket, key, input, metadata)
        }

        // 업로드된 객체의 URL 리턴
        return amazonS3.getUrl(bucket, key).toString()
    }
}

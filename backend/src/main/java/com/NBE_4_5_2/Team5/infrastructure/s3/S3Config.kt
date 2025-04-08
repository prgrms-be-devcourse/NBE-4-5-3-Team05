package com.NBE_4_5_2.Team5.infrastructure.s3

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class S3Config(
    @Value("\${cloud.aws.credentials.access-key}")
    private val accessKey: String,
    @Value("\${cloud.aws.credentials.secret-key}")
    private val secretKey: String,
    @Value("\${cloud.aws.region.static}")
    private val region: String
) {

    @Bean
    fun amazonS3(): com.amazonaws.services.s3.AmazonS3 =
        AmazonS3ClientBuilder
            .standard()
            .withRegion(region)
            .withCredentials(
                AWSStaticCredentialsProvider(
                    BasicAWSCredentials(accessKey, secretKey)
                )
            )
            .build()
}

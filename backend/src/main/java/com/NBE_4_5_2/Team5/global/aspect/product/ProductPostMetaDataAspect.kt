package com.NBE_4_5_2.Team5.global.aspect.product

import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductMetadata
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductMetadataRepository
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class ProductPostMetaDataAspect(
    private val productMetadataRepository: ProductMetadataRepository
) {

    companion object {
        private val log = LoggerFactory.getLogger(ProductPostMetaDataAspect::class.java)
        const val WRITE_POINTCUT =
            "execution(* com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService.write(..))"
        const val DELETE_POINTCUT =
            "execution(* com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService.delete(..))"
    }

    @Around(WRITE_POINTCUT)
    @Throws(Throwable::class)
    fun increaseProductPostCount(joinPoint: ProceedingJoinPoint): Any? {
        log.info("[{}] 게시글 작성 작업 시작", joinPoint.signature.name)
        val byName = productMetadataRepository.findByName(ProductMetaDataNames.PRODUCT_TOTAL_COUNT)
        val metadata: ProductMetadata = if (byName.isEmpty()) {
            log.info("게시글 총 개수 메타데이터가 존재하지 않아 새로 생성")
            productMetadataRepository.save(
                ProductMetadata(ProductMetaDataNames.PRODUCT_TOTAL_COUNT, "0")
            )
        } else {
            log.info("게시글 총 개수 메타데이터를 조회. 현재 값: {}", byName.first().value)
            byName.first()
        }
        val result = joinPoint.proceed()
        metadata.value = (metadata.value.toLong() + 1).toString()
        productMetadataRepository.save(metadata)
        log.info("게시글 작성 완료. 새로운 값: {}", metadata.value)
        return result
    }

    @Around(DELETE_POINTCUT)
    @Throws(Throwable::class)
    fun decreaseProductPostCount(joinPoint: ProceedingJoinPoint): Any? {
        log.info("[{}] 게시글 삭제 작업 시작", joinPoint.signature.name)
        val byName = productMetadataRepository.findByName(ProductMetaDataNames.PRODUCT_TOTAL_COUNT)
        val result = joinPoint.proceed()
        if (byName.isNotEmpty()) {
            val productTotalCount = byName.first()
            log.info("게시글 총 개수 메타데이터를 조회. 현재 값: {}", productTotalCount.value)
            productTotalCount.value = (productTotalCount.value.toLong() - 1).toString()
            productMetadataRepository.save(productTotalCount)
            log.info("게시글 삭제 완료. 새로운 값: {}", productTotalCount.value)
        } else {
            log.warn("게시글 총 개수 메타데이터가 존재하지 않아 업데이트를 생략")
        }
        return result
    }
}

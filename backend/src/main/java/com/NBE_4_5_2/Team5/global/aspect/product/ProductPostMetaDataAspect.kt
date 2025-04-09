package com.NBE_4_5_2.Team5.global.aspect.product

import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductMetadata
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductMetadataRepository
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
@Slf4j
class ProductPostMetaDataAspect(
    private val productMetadataRepository: ProductMetadataRepository
) {

    @Around("execution(* com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService.write(..))")
    @Throws(Throwable::class)
    fun increaseProductPostCount(joinPoint: ProceedingJoinPoint): Any {
        val byName = productMetadataRepository.findByName(
            ProductMetaDataNames.PRODUCT_TOTAL_COUNT
        )
        var save: ProductMetadata? = null
        save = if (byName!!.size == 0) {
            productMetadataRepository.save(
                ProductMetadata(ProductMetaDataNames.PRODUCT_TOTAL_COUNT, "0")
            )
        } else {
            productMetadataRepository.findByName(ProductMetaDataNames.PRODUCT_TOTAL_COUNT)!![0]
        }

        val result = joinPoint.proceed()
        save!!.value = (save.value!!.toLong() + 1).toString()
        productMetadataRepository.save(save)
        return result
    }

    @Around("execution(* com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService.delete(..))")
    @Throws(Throwable::class)
    fun decreaseProductPostCount(joinPoint: ProceedingJoinPoint): Any? {
        val byName = productMetadataRepository.findByName(
            ProductMetaDataNames.PRODUCT_TOTAL_COUNT
        )


        val result = joinPoint.proceed()
        if (byName!!.size == 0) return result

        val productTotalCount = byName[0]
        productTotalCount!!.value = (productTotalCount.value!!.toLong() - 1).toString()
        productMetadataRepository.save(productTotalCount)
        return result
    }
}

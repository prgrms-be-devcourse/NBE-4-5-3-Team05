package com.NBE_4_5_2.Team5.global.aspect.product;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductMetadata;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductMetadataRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductPostMetaDataAspect {

	private final ProductMetadataRepository productMetadataRepository;

	@Around("""
		execution(* com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService.write(..))
		""")
	public Object increaseProductPostCount(ProceedingJoinPoint joinPoint) throws Throwable {
		ProductMetadata productTotalCount = productMetadataRepository.findByName(
			ProductMetaDataNames.PRODUCT_TOTAL_COUNT).get(0);

		Object result = joinPoint.proceed();
		productTotalCount.setValue(String.valueOf(Long.parseLong(productTotalCount.getValue()) + 1));
		productMetadataRepository.save(productTotalCount);
		return result;
	}

	@Around("""
		execution(* com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService.delete(..))
		""")
	public Object decreaseProductPostCount(ProceedingJoinPoint joinPoint) throws Throwable {
		ProductMetadata productTotalCount = productMetadataRepository.findByName(
			ProductMetaDataNames.PRODUCT_TOTAL_COUNT).get(0);

		Object result = joinPoint.proceed();
		productTotalCount.setValue(String.valueOf(Long.parseLong(productTotalCount.getValue()) - 1));
		productMetadataRepository.save(productTotalCount);
		return result;
	}

}

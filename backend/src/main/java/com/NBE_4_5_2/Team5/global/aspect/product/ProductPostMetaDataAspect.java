package com.NBE_4_5_2.Team5.global.aspect.product;

import java.util.List;

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
		List<ProductMetadata> byName = productMetadataRepository.findByName(
			ProductMetaDataNames.PRODUCT_TOTAL_COUNT);
		ProductMetadata save = null;
		if(byName.size() == 0){
			 save = productMetadataRepository.save(
				new ProductMetadata(ProductMetaDataNames.PRODUCT_TOTAL_COUNT, "0"));
		}else{
			save = productMetadataRepository.findByName(ProductMetaDataNames.PRODUCT_TOTAL_COUNT).get(0);
		}

		Object result = joinPoint.proceed();
		save.setValue(String.valueOf(Long.parseLong(save.getValue()) + 1));
		productMetadataRepository.save(save);
		return result;
	}

	@Around("""
		execution(* com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService.delete(..))
		""")
	public Object decreaseProductPostCount(ProceedingJoinPoint joinPoint) throws Throwable {
		List<ProductMetadata> byName = productMetadataRepository.findByName(
			ProductMetaDataNames.PRODUCT_TOTAL_COUNT);


		Object result = joinPoint.proceed();
		if(byName.size() == 0)
			return result;

		ProductMetadata productTotalCount = byName.get(0);
		productTotalCount.setValue(String.valueOf(Long.parseLong(productTotalCount.getValue()) - 1));
		productMetadataRepository.save(productTotalCount);
		return result;
	}

}

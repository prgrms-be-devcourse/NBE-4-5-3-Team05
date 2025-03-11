package com.NBE_4_5_2.Team5.domain.post.post.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.enums.ProductStatus;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;

@Repository
public interface ProductPostRepository extends JpaRepository<ProductPost, String> {

	@EntityGraph(attributePaths = {"productCategories.category"})
	Page<ProductPost> findByTitleLike(String title, Pageable pageable);

	@EntityGraph(attributePaths = {"productCategories.category"})
	@Query("select p from ProductPost p")
	Page<ProductPost> findAllWithCategories(@NonNull Pageable pageable);

	@EntityGraph(attributePaths = {"productCategories.category"})
	Page<ProductPost> findByWriter(User writer, Pageable pageable);

	// 페이징 없이 전체 조회
	@EntityGraph(attributePaths = {"productCategories.category"})
	List<ProductPost> findByWriter(User writer);

	//구매된(판매 완료) 상품들 조회
	@EntityGraph(attributePaths = {"productCategories.category"})
	List<ProductPost> findAllByStatus(ProductStatus status);

	@EntityGraph(attributePaths = {"productCategories.category"})
	List<ProductPost> findByBuyer(User buyer);

	List<ProductPost> findByIdIn(List<String> postIds);
}

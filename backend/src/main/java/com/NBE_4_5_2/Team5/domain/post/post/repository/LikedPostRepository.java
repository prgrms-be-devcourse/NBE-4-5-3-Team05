package com.NBE_4_5_2.Team5.domain.post.post.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.NBE_4_5_2.Team5.domain.post.post.entity.LikedPost;

@Repository
public interface LikedPostRepository extends JpaRepository<LikedPost, Long> {
	@Query("SELECT lp.productPostId FROM LikedPost lp WHERE lp.userId = :userId")
	List<String> findAllProductPostIdsByUserId(String userId);
}

package com.NBE_4_5_2.Team5.domain.post.post.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.NBE_4_5_2.Team5.domain.post.post.dto.response.PreviewPostResponse;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecentlyViewedService {
	private static final String RECENTLY_VIEW_KEY = "recentlyViewed:";
	private final RedisTemplate<String, String> redisTemplate;
	private final ProductPostRepository productPostRepository;

	// 최신순 -> 큐
	public void addViewedPost(String userId, String postId) {
		String key = RECENTLY_VIEW_KEY + userId;
		ListOperations<String, String> listOps = redisTemplate.opsForList();

		listOps.remove(key, 1, postId);
		listOps.leftPush(key, postId);

		System.out.println(listOps.getFirst(key));

		if (listOps.size(key) > 10) {
			listOps.rightPop(key);
		}
	}

	public List<PreviewPostResponse> getRecentlyViewedPosts(String userId) {
		String key = RECENTLY_VIEW_KEY + userId;
		List<String> postIds = Optional.ofNullable(redisTemplate.opsForList().range(key, 0, -1))
			.orElse(Collections.emptyList());

		List<ProductPost> posts = productPostRepository.findByIdIn(postIds);

		return posts.stream().map(PreviewPostResponse::fromEntity).toList();
	}
}

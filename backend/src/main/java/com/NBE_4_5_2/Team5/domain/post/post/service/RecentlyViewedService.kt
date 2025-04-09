package com.NBE_4_5_2.Team5.domain.post.post.service

import com.NBE_4_5_2.Team5.domain.post.post.dto.response.PreviewPostResponse
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class RecentlyViewedService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val productPostRepository: ProductPostRepository
) {
    companion object {
        private const val RECENTLY_VIEW_KEY = "recentlyViewed:"
    }

    fun addViewedPost(userId: String, postId: String) {
        val key = RECENTLY_VIEW_KEY + userId
        val listOps = redisTemplate.opsForList()

        listOps.remove(key, 1, postId)
        listOps.leftPush(key, postId)

        println(listOps.index(key, 0))

        if ((listOps.size(key) ?: 0) > 10) {
            listOps.rightPop(key)
        }
    }

    fun getRecentlyViewedPosts(userId: String): List<PreviewPostResponse> {
        val key = RECENTLY_VIEW_KEY + userId

        val postIds = redisTemplate.opsForList().range(key, 0, -1).orEmpty()
        val posts = productPostRepository.findByIdIn(postIds)

        val postMap = posts.associateBy { it.id }
        val sortedPosts = postIds.mapNotNull { postMap[it] }

        return sortedPosts.map { PreviewPostResponse.fromEntity(it) }
    }
}

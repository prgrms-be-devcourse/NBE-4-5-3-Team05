package com.NBE_4_5_2.Team5.domain.post.post.service;


import com.NBE_4_5_2.Team5.domain.post.post.entity.LikedPost;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductCategory;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.NBE_4_5_2.Team5.domain.post.category.repository.CategoryRepository;
import com.NBE_4_5_2.Team5.domain.post.post.dto.request.ProductPostModifyForm;
import com.NBE_4_5_2.Team5.domain.post.post.dto.request.ProductPostWriteForm;
import com.NBE_4_5_2.Team5.domain.post.post.dto.response.LocationResponse
import com.NBE_4_5_2.Team5.domain.post.post.dto.response.PreviewPostResponse;
import com.NBE_4_5_2.Team5.domain.post.post.dto.response.ProductPostResponse;
import com.NBE_4_5_2.Team5.domain.post.post.enums.ProductStatus;
import com.NBE_4_5_2.Team5.domain.post.post.repository.LikedPostRepository;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.global.dto.PageDto;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;
import com.NBE_4_5_2.Team5.global.exception.post.category.CategoryNotFoundException
import com.NBE_4_5_2.Team5.global.exception.post.product.ProductPostNotFoundException;
import com.NBE_4_5_2.Team5.global.exception.post.product.PurchasedProductException;
import com.NBE_4_5_2.Team5.global.exception.security.ForbiddenAccessException;
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


@Service
class ProductPostService(
    val productPostRepository: ProductPostRepository,
    val categoryRepository: CategoryRepository,
    val likedPostRepository: LikedPostRepository,
) {

    @Transactional
    fun write(actor: User, body: ProductPostWriteForm): ProductPostResponse {
        val imageUrlStr = body.imageUrlList.joinToString(",")

        // 글 작성
        val productPost = ProductPost(
            actor,
            body.productName,
            body.productPrice,
            body.title,
            body.content,
            imageUrlStr,
            body.latitude,
            body.longitude,
            body.location
        )

        // 상품글에 카테고리 체크 및 추가
        val reqCategoryIdList = body.categoryIds
        val realCategoryList = categoryRepository.findAllById(reqCategoryIdList)
        if (realCategoryList.size != reqCategoryIdList.size) {
            throw CategoryNotFoundException("400", "존재하지 않는 카테고리가 포함되어있습니다.");
        }
        productPost.addCategories(realCategoryList)

        val saved = productPostRepository.save(productPost)

        actor.addWrittenPost(saved)

        return ProductPostResponse.fromEntity(productPost)
    }

    @Transactional
    fun getPosts(
        page: Int,
        pageSize: Int,
        keyword: String,
        sort: String,
        minPrice: Int,
        maxPrice: Int,
        categoryIds: List<Long>
    ): PageDto<PreviewPostResponse> {
        val sortDirection = if (sort.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(
            page - 1, pageSize,
            Sort.by(sortDirection, "createdDate")
        )

        val resultPage = if (keyword.isBlank()) {
            if (categoryIds.isEmpty()) {
                productPostRepository.findAllWithPrice(minPrice, maxPrice, pageable)
            } else {
                productPostRepository.findAllWithFilters(minPrice, maxPrice, categoryIds, categoryIds.size.toLong(), pageable)
            }
        } else {
            if (categoryIds.isEmpty()) {
                productPostRepository.findByKeywordWithPrice("%$keyword%", minPrice, maxPrice, pageable)
            } else {
                productPostRepository.findByKeywordWithFilters("%$keyword%", minPrice, maxPrice, categoryIds, categoryIds.size.toLong(), pageable)
            }
        }


        val mappedPosts = resultPage.map { post ->
            val likedCount = likedPostRepository.countByProductPostId(post.id)
            PreviewPostResponse.fromEntityWithLikeCount(post, likedCount)
        }

        return PageDto(mappedPosts)
    }

    fun getMyPosts(
        actor: User, page: Int, pageSize: Int, sort: String, status: ProductStatus?
    ): PageDto<PreviewPostResponse> {
        val sortDirection = if (sort.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page - 1, pageSize, Sort.by(sortDirection, "createdDate"))

        val postPage = if (status != null) {
            productPostRepository.findByWriterAndStatus(actor, status, pageable)
        } else {
            productPostRepository.findByWriter(actor, pageable)
        }

        val mappedMyPosts = postPage.map { PreviewPostResponse.fromEntity(it) }
        return PageDto(mappedMyPosts)
    }

    fun getPost(id: String): ProductPostResponse {
        val post = productPostRepository.findById(id)
            .orElseThrow { ProductPostNotFoundException("404", "해당 글은 존재하지 않습니다.") }

        post.incrementViewCount()
        productPostRepository.save(post)

        val likedCount = likedPostRepository.countByProductPostId(id)
        return ProductPostResponse.fromEntityWithLikeCount(post, likedCount)
    }


    fun delete(actor: User, postId: String) {
        val post = productPostRepository.findById(postId)
            .orElseThrow { ProductPostNotFoundException("404", "해당 글은 존재하지 않습니다.") }

        post.canDelete(actor)

        productPostRepository.delete(post)
    }

    @Transactional
    fun modify(actor: User, postId: String, body: ProductPostModifyForm): ProductPostResponse {
        val post = productPostRepository.findById(postId)
            .orElseThrow { ProductPostNotFoundException("404", "해당 글은 존재하지 않습니다.") }

        post.canModify(actor)

        body.productName?.let { post.productName = it }
        body.productPrice?.let { post.productPrice = it }
        body.title?.let { post.title = it }
        body.content?.let { post.content = it }
        body.imageUrlList?.takeIf { it.isNotEmpty() }?.let { post.imageUrls = it.joinToString(",") }
        body.latitude?.let { post.latitude = it }
        body.longitude?.let { post.longitude = it }
        body.status?.let { post.status = it }
        body.location?.let { post.location = it }

        body.categoryIds?.let {
            val categories = categoryRepository.findAllById(it)
            post.productCategories.clear()
            val newCategories = categories.map { category -> ProductCategory.of(post, category) }
            post.productCategories.addAll(newCategories)
        }

        return ProductPostResponse.fromEntity(post)
    }

    // 찜 기능: 한 유저가 한 게시글에 대해 찜을 한 번만 할 수 있도록 한다.
    @Transactional
    fun likePost(actor: User, postId: String): ProductPostResponse {
        val post = productPostRepository.findByIdWithWriter(postId)
            .orElseThrow { ServiceException("404", "해당 글은 존재하지 않습니다.") }

        if (likedPostRepository.existsByUserIdAndProductPostId(actor.id, postId)) {
            throw ServiceException("400", "이미 찜한 게시글입니다.")
        }

        val likedPost = LikedPost.of(actor.id, postId)
        likedPostRepository.save(likedPost)

        val likedCount = likedPostRepository.countByProductPostId(postId)
        return ProductPostResponse.fromEntityWithLikeCount(post, likedCount)
    }

    // 특정 게시글을 로그인 유저가 구매 확정

    fun purchasePost(buyer: User, postId: String): ProductPostResponse {
        val post = productPostRepository.findById(postId)
            .orElseThrow { ProductPostNotFoundException("404", "해당 글은 존재하지 않습니다.") }

        if (post.status == ProductStatus.PURCHASED) {
            throw PurchasedProductException("400", "이미 판매 완료된 상품입니다.")
        }

        if (post.writer == buyer) {
            throw ForbiddenAccessException("403", "자신이 작성한 상품을 구매할 수 없습니다.")
        }

        post.purchaseBy(buyer)
        productPostRepository.save(post)

        return ProductPostResponse.fromEntity(post)
    }

    //내가 구매한 내역
    @Transactional(readOnly = true)
    fun getMyPurchases(actor: User, page: Int, pageSize: Int): PageDto<PreviewPostResponse> {
        val pageable = PageRequest.of(page - 1, pageSize)
        val purchasedPosts = productPostRepository.findByBuyer(actor, pageable)
        val mappedPosts = purchasedPosts.map { PreviewPostResponse.fromEntity(it) }
        return PageDto(mappedPosts)
    }

    fun getMySales(actor: User): List<ProductPostResponse> {
        val salesPosts = productPostRepository.findByWriter(actor)
        return salesPosts.map { ProductPostResponse.fromEntity(it) }
    }

    fun getMyFavorites(actor: User, page: Int, pageSize: Int): PageDto<PreviewPostResponse> {
        val pageable = PageRequest.of(page - 1, pageSize)
        val postIds = likedPostRepository.findAllProductPostIdsByUserId(actor.id)
        val favoritePosts = productPostRepository.findByIdIn(postIds, pageable)
        val mappedPosts = favoritePosts.map { PreviewPostResponse.fromEntity(it) }
        return PageDto(mappedPosts)
    }

    @Transactional
    fun updateStatus(status: ProductStatus, postId: String) {
        val productPost = productPostRepository.findById(postId)
            .orElseThrow { ProductPostNotFoundException("404", "해당 글은 존재하지 않습니다.") }
        productPost.updateStatus(status)
    }

    @Transactional(readOnly = true)
    fun getPostByLocation(lat: Float, lng: Float, sort: String, radius:Double, page: Int, pageSize: Int): PageDto<LocationResponse> {
        val sortDirection = if (sort.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page - 1, pageSize, Sort.by(sortDirection, "createdDate"))
        val posts = productPostRepository.findAll()

        // 거리계산 필터링
        val filterPosts = posts.filter { post ->
            println("게시글 번호: ${post.id}")
            println("게시글 위치!!!!!: lat = ${post.latitude}, lng = ${post.longitude}")
            val distance = haversine(lat,lng,post.latitude,post.longitude)
            post.distance = distance
            println("거리!!!!!: $distance")
            distance <= radius
        }


        // 페이징
        val start = pageable.pageSize * pageable.pageNumber
        val end = Math.min(start + pageable.pageSize, filterPosts.size)
        val pagedPosts = filterPosts.subList(start, end)

        val mappedPosts = pagedPosts.map { LocationResponse.fromEntity(it) }

        return PageDto(
            items = mappedPosts,
            totalPages = (filterPosts.size + pageSize - 1) / pageSize, // 전체 페이지 수
            totalItems = filterPosts.size, // 전체 게시글 수
            currentPageNo = page, // 현재 페이지 번호
            pageSize = pageSize // 페이지 크기
        )
    }

    // 대충 거리 계산하는 알고리즘(단위는 km)
    private fun haversine(lat: Float, lng: Float, postLat: Float, postLng: Float): Double {
        val R = 6371.0 // 지구 반지름 (km)

        // 위도와 경도를 라디안으로 변환
        val latRad = Math.toRadians(lat.toDouble())
        val lngRad = Math.toRadians(lng.toDouble())
        val postLatRad = Math.toRadians(postLat.toDouble())
        val postLngRad = Math.toRadians(postLng.toDouble())

        // 위도와 경도의 차이 계산
        val dLat = postLatRad - latRad
        val dLng = postLngRad - lngRad

        // Haversine 공식 계산
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(latRad) * cos(postLatRad) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        // 거리 계산
        return R * c
    }
}

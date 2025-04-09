package com.NBE_4_5_2.Team5.domain.post.post.controller

import com.NBE_4_5_2.Team5.domain.post.post.dto.request.ProductPostModifyForm
import com.NBE_4_5_2.Team5.domain.post.post.dto.request.ProductPostWriteForm
import com.NBE_4_5_2.Team5.domain.post.post.dto.response.PreviewPostResponse
import com.NBE_4_5_2.Team5.domain.post.post.dto.response.ProductPostResponse
import com.NBE_4_5_2.Team5.domain.post.post.enums.ProductStatus
import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService
import com.NBE_4_5_2.Team5.domain.post.post.service.RecentlyViewedService
import com.NBE_4_5_2.Team5.domain.user.user.service.UserAuthService
import com.NBE_4_5_2.Team5.global.dto.Empty
import com.NBE_4_5_2.Team5.global.dto.PageDto
import com.NBE_4_5_2.Team5.global.response.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import lombok.RequiredArgsConstructor
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Product Post API", description = "상품 게시글 API")
class ProductPostController(
    private val productPostService: ProductPostService,
    private val recentlyViewedService: RecentlyViewedService,
    private val userAuthService: UserAuthService
) {

    @Operation(summary = "상품 게시글 작성", description = "상품 게시글을 작성합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    fun createPost(@Valid @RequestBody body: ProductPostWriteForm): RsData<ProductPostResponse> {
        val actor = userAuthService.userIdentity
        val postResponse = productPostService.write(actor, body)
        return RsData("200", "글 작성 성공", postResponse)
    }

    @Operation(summary = "글 목록 조회", description = "상품 게시글 목록을 조회합니다.")
    @GetMapping
    @Transactional(readOnly = true)
    fun getPosts(
        @Parameter(description = "페이지 번호")
        @RequestParam(defaultValue = "1") page: Int,
        @Parameter(description = "페이지에 포함된 아이템 개수")
        @RequestParam(defaultValue = "10") pageSize: Int,
        @Parameter(description = "검색 키워드")
        @RequestParam(defaultValue = "") keyword: String,
        @Parameter(description = "정렬 순서. desc:내림차순, asc:오름차순", example = "desc")
        @RequestParam(defaultValue = "desc") sort: String,
        @RequestParam(defaultValue = "0") minPrice: Int,
        @RequestParam(defaultValue = "${Int.MAX_VALUE}") maxPrice: Int,
        @RequestParam(name = "categoryIds", required = false) categoryIds: List<Long> = emptyList()
    ): RsData<PageDto<PreviewPostResponse>> {
        val postPage = productPostService.getPosts(
            page, pageSize, keyword, sort, minPrice, maxPrice, categoryIds
        )
        return RsData("200", "글 목록 조회가 완료되었습니다.", postPage)
    }

    @Operation(summary = "내가 작성한 상품 게시글 조회", description = "내가 작성한 상품 게시글을 조회합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    @Transactional(readOnly = true)
    fun getMyPosts(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "desc") sort: String,
        @RequestParam(required = false) status: ProductStatus?
    ): RsData<PageDto<PreviewPostResponse>> {
        val actor = userAuthService.userIdentity
        val postPage = productPostService.getMyPosts(actor, page, pageSize, sort, status)
        return RsData("200", "내 글 목록 조회가 완료되었습니다.", postPage)
    }

    @Operation(summary = "상품 게시글 상세 조회", description = "상품 게시글의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    @Transactional(readOnly = false)
    fun getPost(
        @Parameter(description = "상품 게시글 id", example = "ppost-f90sdf8-sd8fu7sd-ds8uf9")
        @PathVariable id: String
    ): RsData<ProductPostResponse> {
        val postResponse = productPostService.getPost(id)

        try {
            val user = userAuthService.userIdentity
            recentlyViewedService.addViewedPost(user.id, id)
        } catch (e: Exception) {
            // 로그인 안 한 경우 무시
        }

        return RsData("200", "게시물 조회가 완료되었습니다.", postResponse)
    }

    @Operation(summary = "최근 조회한 상품 조회", description = "최근 조회한 상품들을 조회합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/recently-viewed")
    @Transactional(readOnly = true)
    fun getRecentlyViewPosts(): RsData<List<PreviewPostResponse>> {
        val user = userAuthService.userIdentity
        val recentlyViewedPosts = recentlyViewedService.getRecentlyViewedPosts(user.id)
        return RsData("200", "최근 본 상품 목록 조회가 완료되었습니다.", recentlyViewedPosts)
    }

    @Operation(summary = "상품 게시글 수정", description = "상품 게시글의 내용을 수정합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @Transactional
    fun modify(
        @Valid @RequestBody body: ProductPostModifyForm,
        @PathVariable id: String
    ): RsData<ProductPostResponse> {
        val actor = userAuthService.userIdentity
        val postResponse = productPostService.modify(actor, id, body)
        return RsData("200", "글 수정 완료.", postResponse)
    }

    @Operation(summary = "상품 게시글 삭제", description = "상품 게시글을 삭제합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Transactional
    fun delete(
        @PathVariable id: String
    ): RsData<Empty> {
        val actor = userAuthService.userIdentity
        productPostService.delete(actor, id)
        return RsData("200", "글 삭제 완료.")
    }

    @Operation(summary = "상품 게시글 찜", description = "상품 게시글을 찜합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/like")
    fun likePost(@PathVariable id: String): RsData<ProductPostResponse> {
        val actor = userAuthService.userIdentity
        val response = productPostService.likePost(actor, id)
        return RsData("200", "찜 완료", response)
    }

    @Operation(summary = "내가 구매한 상품 게시글 리스트 조회", description = "내가 구매한 상품 게시글의 목록을 조회합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my/purchases")
    fun getMyPurchases(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int
    ): RsData<PageDto<PreviewPostResponse>> {
        val actor = userAuthService.userIdentity
        val myPurchases = productPostService.getMyPurchases(actor, page, pageSize)
        return RsData("200", "내 구매 내역 조회 성공", myPurchases)
    }

    @Operation(summary = "내가 판매한 상품 게시글 리스트 조회", description = "내가 판매한 상품 게시글의 목록을 조회합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my/sales")
    fun getMySales(): RsData<List<ProductPostResponse>> {
        val actor = userAuthService.userIdentity
        val sales = productPostService.getMySales(actor)
        return RsData("200", "내 판매 내역 조회 성공", sales)
    }

    @Operation(summary = "내가 찜한 상품 게시글 리스트 조회", description = "내가 찜한 상품 게시글의 목록을 조회합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my/favorites")
    fun getMyFavorites(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int
    ): RsData<PageDto<PreviewPostResponse>> {
        val actor = userAuthService.userIdentity
        val favorites = productPostService.getMyFavorites(actor, page, pageSize)
        return RsData("200", "내가 찜한 내역 조회 성공", favorites)
    }
}

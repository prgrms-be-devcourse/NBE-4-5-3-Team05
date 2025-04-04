package com.NBE_4_5_2.Team5.domain.post.post.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.NBE_4_5_2.Team5.domain.post.post.dto.request.ProductPostModifyForm;
import com.NBE_4_5_2.Team5.domain.post.post.dto.request.ProductPostWriteForm;
import com.NBE_4_5_2.Team5.domain.post.post.dto.response.PreviewPostResponse;
import com.NBE_4_5_2.Team5.domain.post.post.dto.response.ProductPostResponse;
import com.NBE_4_5_2.Team5.domain.post.post.enums.ProductStatus;
import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService;
import com.NBE_4_5_2.Team5.domain.post.post.service.RecentlyViewedService;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserAuthService;
import com.NBE_4_5_2.Team5.global.dto.Empty;
import com.NBE_4_5_2.Team5.global.dto.PageDto;
import com.NBE_4_5_2.Team5.global.dto.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Product Post API", description = "상품 게시글 API")
public class ProductPostController {
	private final ProductPostService productPostService;
	private final RecentlyViewedService recentlyViewedService;
	private final UserAuthService userAuthService;

	@Operation(summary = "상품 게시글 작성", description = "상품 게시글을 작성합니다.")
	@SecurityRequirement(name = "cookieAuth")
	@PreAuthorize("isAuthenticated()")
	@PostMapping
	public RsData<ProductPostResponse> createPost(@Valid @RequestBody ProductPostWriteForm body) {

		User actor = userAuthService.getUserIdentity();
		ProductPostResponse postResponse = productPostService.write(actor, body);

		return new RsData<>(
			"200",
			"글 작성 성공",
			postResponse
		);
	}

	@Operation(summary = "글 목록 조회", description = "상품 게시글 목록을 조회합니다.")
	@GetMapping
	@Transactional(readOnly = true)
	public RsData<PageDto<PreviewPostResponse>> getPosts(
		@Parameter(description = "페이지 번호")
		@RequestParam(defaultValue = "1") int page,
		@Parameter(description = "페이지에 포함된 아이템 개수")
		@RequestParam(defaultValue = "10") int pageSize,
		@Parameter(description = "검색 키워드")
		@RequestParam(defaultValue = "") String keyword,
		@Parameter(description = "정렬 순서. desc:내림차순, asc:오름차순", example = "desc")
		@RequestParam(defaultValue = "desc") String sort) {
		PageDto<PreviewPostResponse> postPage = productPostService.getPosts(page, pageSize, keyword, sort);

		return new RsData<>(
			"200",
			"글 목록 조회가 완료되었습니다.",
			postPage
		);
	}

	@Operation(summary = "내가 작성한 상품 게시글 조회", description = "내가 작성한 상품 게시글을 조회합니다.")
	@SecurityRequirement(name = "cookieAuth")
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/my")
	@Transactional(readOnly = true)
	public RsData<PageDto<PreviewPostResponse>> getMyPosts(
		@Parameter(description = "페이지 번호")
		@RequestParam(defaultValue = "1") int page,
		@Parameter(description = "페이지 내 아이템 개수")
		@RequestParam(defaultValue = "10") int pageSize,
		@Parameter(description = "정렬 순서")
		@RequestParam(defaultValue = "desc") String sort,
		@RequestParam(required = false) ProductStatus status) {
		User actor = userAuthService.getUserIdentity();
		PageDto<PreviewPostResponse> postPage = productPostService.getMyPosts(actor, page, pageSize, sort, status);

		return new RsData<>(
			"200",
			"내 글 목록 조회가 완료되었습니다.",
			postPage
		);
	}

	@Operation(summary = "상품 게시글 상세 조회", description = "상품 게시글의 상세 정보를 조회합니다.")
	@GetMapping("/{id}")
	@Transactional(readOnly = false)
	public RsData<ProductPostResponse> getPost(
		@Parameter(description = "상품 게시글 id", example = "ppost-f90sdf8-sd8fu7sd-ds8uf9")
		@PathVariable String id) {
		// 인증되지 않은 경우에도 게시글 상세 조회가 가능하도록 수정
		ProductPostResponse postResponse = productPostService.getPost(id);

		// 만약 현재 로그인된 사용자가 있다면 최근 본 게시글로 추가
		try {
			User user = userAuthService.getUserIdentity();
			recentlyViewedService.addViewedPost(user.getId(), id);
		} catch (Exception e) {
			// 로그인 정보가 없으면 그냥 넘어감 (혹은 로그로 남김)
		}

		return new RsData<>(
			"200",
			"게시물 조회가 완료되었습니다.",
			postResponse
		);
	}

	@Operation(summary = "최근 조회한 상품 조회", description = "최근 조회한 상품들을 조회합니다.")
	@SecurityRequirement(name = "cookieAuth")
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/recently-viewed")
	@Transactional(readOnly = true)
	public RsData<List<PreviewPostResponse>> getRecentlyViewPosts() {

		User user = userAuthService.getUserIdentity();
		List<PreviewPostResponse> recentlyViewedPosts = recentlyViewedService.getRecentlyViewedPosts(user.getId());

		return new RsData<>(
			"200",
			"최근 본 상품 목록 조회가 완료되었습니다.",
			recentlyViewedPosts
		);
	}

	@Operation(summary = "상품 게시글 수정", description = "상품 게시글의 내용을 수정합니다.")
	@SecurityRequirement(name = "cookieAuth")
	@PreAuthorize("isAuthenticated()")
	@PutMapping("/{id}")
	@Transactional
	public RsData<ProductPostResponse> modify(
		@Valid @RequestBody ProductPostModifyForm body,
		@Parameter(description = "상품 게시글 id", example = "ppost-fsiodf-21edd-fd2c1")
		@PathVariable String id) {

		User actor = userAuthService.getUserIdentity();
		ProductPostResponse postResponse = productPostService.modify(actor, id, body);

		return new RsData<>(
			"200",
			"글 수정 완료.",
			postResponse
		);
	}

	@Operation(summary = "상품 게시글 삭제", description = "상품 게시글을 삭제합니다.")
	@SecurityRequirement(name = "cookieAuth")
	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/{id}")
	@Transactional
	public RsData<Empty> delete(
		@Parameter(description = "상품 게시글 id", example = "ppost-2ji109-fe3sfd-3fsdf")
		@PathVariable String id) {

		User actor = userAuthService.getUserIdentity();
		productPostService.delete(actor, id);

		return new RsData<>(
			"200",
			"글 삭제 완료."
		);
	}

	/// 찜(좋아요) 엔드포인트 – 한 유저가 한 게시글에 대해 한 번만 찜할 수 있음
	@Operation(summary = "상품 게시글 찜", description = "상품 게시글을 찜합니다.")
	@SecurityRequirement(name = "cookieAuth")
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/{id}/like")
	public RsData<ProductPostResponse> likePost(
		@Parameter(description = "상품 게시글 id", example = "ppost-2ji109-fe3sfd-3fsdf")
		@PathVariable String id) {
		User actor = userAuthService.getUserIdentity();
		ProductPostResponse response = productPostService.likePost(actor, id);
		return new RsData<>("200", "찜 완료", response);
	}

	/// 내가 구매한 내역 조회
	@Operation(summary = "내가 구매한 상품 게시글 리스트 조회", description = "내가 구매한 상품 게시글의 목록을 조회합니다.")
	@SecurityRequirement(name = "cookieAuth")
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/my/purchases")
	public RsData<PageDto<ProductPostResponse>> getMyPurchases(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int pageSize
	) {
		User actor = userAuthService.getUserIdentity();

		PageDto<ProductPostResponse> myPurchases = productPostService.getMyPurchases(actor, page, pageSize);

		return new RsData<>(
			"200",
			"내 구매 내역 조회 성공",
			myPurchases
		);
	}

	/// 내가 판매한 내역
	@Operation(summary = "내가 판매한 상품 게시글 리스트 조회", description = "내가 판매한 상품 게시글의 목록을 조회합니다.")
	@SecurityRequirement(name = "cookieAuth")
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/my/sales")
	public RsData<List<ProductPostResponse>> getMySales() {
		User actor = userAuthService.getUserIdentity();
		List<ProductPostResponse> sales = productPostService.getMySales(actor);

		return new RsData<>(
			"200",
			"내 판매 내역 조회 성공",
			sales
		);
	}

	/// 내가 찜한 내역
	@Operation(summary = "내가 판매한 상품 게시글 리스트 조회", description = "내가 판매한 상품 게시글의 목록을 조회합니다.")
	@SecurityRequirement(name = "cookieAuth")
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/my/favorites")
	public RsData<PageDto<ProductPostResponse>> getMyFavorites(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int pageSize
	) {
		User actor = userAuthService.getUserIdentity();
		PageDto<ProductPostResponse> favorites = productPostService.getMyFavorites(actor, page, pageSize);

		return new RsData<>(
			"200",
			"내가 찜한 내역 조회 성공",
			favorites
		);
	}
}

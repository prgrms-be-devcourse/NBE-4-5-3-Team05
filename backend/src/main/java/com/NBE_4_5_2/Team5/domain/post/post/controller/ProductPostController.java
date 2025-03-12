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
import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService;
import com.NBE_4_5_2.Team5.domain.post.post.service.RecentlyViewedService;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.global.Rq;
import com.NBE_4_5_2.Team5.global.dto.Empty;
import com.NBE_4_5_2.Team5.global.dto.PageDto;
import com.NBE_4_5_2.Team5.global.dto.RsData;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class ProductPostController {
	private final ProductPostService productPostService;
	private final RecentlyViewedService recentlyViewedService;
	private final Rq rq;

	@PreAuthorize("isAuthenticated()")
	@PostMapping
	public RsData<ProductPostResponse> createPost(@Valid @RequestBody ProductPostWriteForm body) {

		User actor = rq.getUserIdentity();
		ProductPostResponse postResponse = productPostService.write(actor, body);

		return new RsData<>(
			"200",
			"글 작성 성공",
			postResponse
		);
	}

	@GetMapping
	@Transactional(readOnly = true)
	public RsData<PageDto<PreviewPostResponse>> getPosts(@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int pageSize,
		@RequestParam(defaultValue = "") String keyword,
		@RequestParam(defaultValue = "desc") String sort) {
		PageDto<PreviewPostResponse> postPage = productPostService.getPosts(page, pageSize, keyword, sort);

		return new RsData<>(
			"200",
			"글 목록 조회가 완료되었습니다.",
			postPage
		);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/my")
	@Transactional(readOnly = true)
	public RsData<PageDto<PreviewPostResponse>> getMyPosts(@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int pageSize,
		@RequestParam(defaultValue = "desc") String sort) {
		User actor = rq.getUserIdentity();
		PageDto<PreviewPostResponse> postPage = productPostService.getMyPosts(actor, page, pageSize, sort);

		return new RsData<>(
			"200",
			"내 글 목록 조회가 완료되었습니다.",
			postPage
		);
	}

	@GetMapping("/{id}")
	@Transactional(readOnly = true)
	public RsData<ProductPostResponse> getPost(@PathVariable String id) {
		// 인증되지 않은 경우에도 게시글 상세 조회가 가능하도록 수정
		ProductPostResponse postResponse = productPostService.getPost(id);

		// 만약 현재 로그인된 사용자가 있다면 최근 본 게시글로 추가
		try {
			User user = rq.getUserIdentity();
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

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/recently-viewed")
	@Transactional(readOnly = true)
	public RsData<List<PreviewPostResponse>> getRecentlyViewPosts() {

		User user = rq.getUserIdentity();
		List<PreviewPostResponse> recentlyViewedPosts = recentlyViewedService.getRecentlyViewedPosts(user.getId());

		return new RsData<>(
			"200",
			"최근 본 상품 목록 조회가 완료되었습니다.",
			recentlyViewedPosts
		);
	}

	@PreAuthorize("isAuthenticated()")
	@PutMapping("/{id}")
	@Transactional
	public RsData<ProductPostResponse> modify(
		@Valid @RequestBody ProductPostModifyForm body,
		@PathVariable String id) {

		User actor = rq.getUserIdentity();
		ProductPostResponse postResponse = productPostService.modify(actor, id, body);

		return new RsData<>(
			"200",
			"글 수정 완료.",
			postResponse
		);
	}

	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/{id}")
	@Transactional
	public RsData<Empty> delete(@PathVariable String id) {

		User actor = rq.getUserIdentity();
		productPostService.delete(actor, id);

		return new RsData<>(
			"200",
			"글 삭제 완료."
		);
	}

    // 내가 구매한 내역 조회
    @GetMapping("/my/purchases")
    public RsData<List<ProductPostResponse>> getMyPurchases() {
        User actor = rq.getUserIdentity();

		List<ProductPostResponse> myPurchases = productPostService.getMyPurchases(actor);

		return new RsData<>(
			"200",
			"내 구매 내역 조회 성공",
			myPurchases
		);
	}

	// 내가 판매한 내역
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/my/sales")
	public RsData<List<ProductPostResponse>> getMySales() {
		User actor = rq.getUserIdentity();
		List<ProductPostResponse> sales = productPostService.getMySales(actor);

		return new RsData<>(
			"200",
			"내 판매 내역 조회 성공",
			sales
		);
	}

	// 내가 찜한 내역
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/my/favorites")
	public RsData<List<ProductPostResponse>> getMyFavorites() {
		User actor = rq.getUserIdentity();
		List<ProductPostResponse> favorites = productPostService.getMyFavorites(actor);

		return new RsData<>(
			"200",
			"내가 찜한 내역 조회 성공",
			favorites
		);
	}
}

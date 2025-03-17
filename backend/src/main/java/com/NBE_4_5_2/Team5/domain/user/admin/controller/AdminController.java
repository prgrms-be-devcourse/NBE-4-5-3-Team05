package com.NBE_4_5_2.Team5.domain.user.admin.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.NBE_4_5_2.Team5.domain.user.admin.dto.BanListDto;
import com.NBE_4_5_2.Team5.domain.user.admin.dto.BanResBody;
import com.NBE_4_5_2.Team5.domain.user.admin.dto.NoticeResBody;
import com.NBE_4_5_2.Team5.domain.user.admin.entity.NoticePost;
import com.NBE_4_5_2.Team5.domain.user.admin.service.AdminService;
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserDto;
import com.NBE_4_5_2.Team5.global.response.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Tag(name = "Admin API", description = "관리자 전용 API")
public class AdminController {

	private final AdminService adminService;

	public record NoticeReqBody(@NotEmpty
								@Parameter(description = "공지사항 제목", example = "서비스 점검 안내") String title,
								@NotEmpty
								@Parameter(description = "공지사항 내용",
									example = "서비스 점검으로 인해 3월 15일 02시부터 04시까지 이용이 제한됩니다.") String content) {
	}

	@Operation(summary = "공지사항 등록", description = "새로운 공지사항을 등록합니다.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "공지사항 등록 성공")})
	@SecurityRequirement(name = "cookieAuth")
	@PreAuthorize("isAuthenticated")
	@PostMapping("/notices")
	public RsData<NoticeResBody> writeNotice(
		@Parameter(description = "공지사항 등록 body")
		@RequestBody @Valid NoticeReqBody body) {
		NoticeResBody data = adminService.writeNotice(body.title(), body.content());

		return new RsData<>("200-1", "공지사항 등록 성공.", data);
	}

	public record BanReqBody(@NotEmpty String reason) {
	}

	@Operation(summary = "유저 정지", description = "특정 유저를 정지시킵니다.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "유저 정지 성공")})
	@SecurityRequirement(name = "cookieAuth")
	@PreAuthorize("isAuthenticated")
	@PostMapping("/users/{user-id}/ban")
	public RsData<BanResBody> banUser(
		@Parameter(description = "유저 id")
		@PathVariable(name = "user-id") String userId,
		@Valid
		@Parameter(description = "공지사항 등록 요청 바디")
		@RequestBody BanReqBody reason) {
		BanListDto res = adminService.banUser(userId, reason.reason());
		return new RsData<>("200-1", "유저 정지 성공",
			new BanResBody(res.getId(), userId, reason.reason(), res.getUser().getBlockedCount(), res.getStartDate(),
				res.getEndDate()));
	}

	@PreAuthorize("isAuthenticated")
	@DeleteMapping("/users/{user-id}/ban")
	public RsData<Void> unBanUser(@PathVariable(name = "user-id") String userId) {
		adminService.unBanUser(userId);
		return new RsData<>("204-1", "계정 정지 해제 성공.");
	}

	@Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
	@ApiResponses(value = {@ApiResponse(responseCode = "204", description = "게시글 삭제 성공")})
	@SecurityRequirement(name = "cookieAuth")
	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/posts/{post-id}")
	public RsData<Void> deletePost(
		@Parameter(description = "post id")
		@PathVariable(name = "post-id") String postId) {
		adminService.deletePost(postId);
		return new RsData<>("204-1", "게시글 삭제 성공.");
	}

	@Operation(summary = "최신 공지사항 조회", description = "최신 공지사항 5개를 조회합니다.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "최신 공지사항 조회 성공")})
	// 최신 공지사항 5개 조회 엔드포인트 추가
	@GetMapping("/notices/latest")
	public RsData<List<NoticeResBody>> getLatestNotices() {
		List<NoticePost> latestNotices = adminService.getLatestNotices(5);
		List<NoticeResBody> res = latestNotices.stream()
			.map(NoticeResBody::of)
			.collect(Collectors.toList());
		return new RsData<>("200", "최신 공지사항 조회 성공.", res);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/users")
	public RsData<Page<UserDto>> getUserList(@PageableDefault(size = 10, page = 0) Pageable pageable) {
		Page<UserDto> users = adminService.getUsers(pageable);

		return new RsData<>("200-1", "유저 리스트 조회 성공.", users);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/notices")
	public RsData<Page<NoticeResBody>> getNotices(@PageableDefault(size = 10, page = 0) Pageable pageable) {
		Page<NoticeResBody> notices = adminService.getNotices(pageable);
		return new RsData<>("200-1", "공지사항 리스트 조회 성공.", notices);
	}

	public record UpdateNoticeReq(String title, String content) {
	}

	@PreAuthorize("isAuthenticated()")
	@PutMapping("/notices/{notice-id}")
	public RsData<NoticeResBody> updateNotice(@PathVariable(name = "notice-id") String noticeId,
		@RequestBody UpdateNoticeReq body) {
		NoticeResBody res = adminService.updateNotice(noticeId, body);
		return new RsData<>("200-1", "공지사항 업데이트 성공.", res);
	}

	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/notices/{notice-id}")
	public RsData<Void> deleteNotice(@PathVariable(name = "notice-id") String noticeId) {
		adminService.deleteNotice(noticeId);
		return new RsData<>("200-1", "공지사항 삭제 완료.");
	}

	@GetMapping("/notices/{notice-id}")
	public RsData<NoticeResBody> getNotice(@PathVariable(name = "notice-id") String noticeId) {
		NoticeResBody noticeDto = adminService.getNotice(noticeId);

		return new RsData<>("200-1", "notice 삭제 성공.", noticeDto);
	}
}

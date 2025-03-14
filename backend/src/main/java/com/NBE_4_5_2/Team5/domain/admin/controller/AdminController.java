package com.NBE_4_5_2.Team5.domain.admin.controller;

import com.NBE_4_5_2.Team5.domain.admin.entity.NoticePost;
import org.springframework.web.bind.annotation.*;

import com.NBE_4_5_2.Team5.domain.admin.dto.BanListDto;
import com.NBE_4_5_2.Team5.domain.admin.dto.BanResBody;
import com.NBE_4_5_2.Team5.domain.admin.dto.NoticeResBody;
import com.NBE_4_5_2.Team5.domain.admin.service.AdminService;
import com.NBE_4_5_2.Team5.global.response.RsData;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

	private final AdminService adminService;

	public record NoticeReqBody(@NotEmpty String title, @NotEmpty String content) {
	}

	@PostMapping("/notices")
	public RsData<NoticeResBody> writeNotice(@RequestBody @Valid NoticeReqBody body) {
		NoticeResBody data = adminService.writeNotice(body.title(), body.content());

		return new RsData<>("200-1", "공지사항 등록 성공.", data);
	}

	public record BanReqBody(@NotEmpty String reason) {
	}

	@PostMapping("/users/{user-id}/ban")
	public RsData<BanResBody> banUser(@PathVariable(name = "user-id") String userId,
		@Valid @RequestBody BanReqBody reason) {
		BanListDto res = adminService.banUser(userId, reason.reason());
		return new RsData<>("200-1", "유저 정지 성공", new BanResBody(
			res.getId(),
			userId,
			reason.reason(),
			res.getUser().getBlockedCount(),
			res.getStartDate(),
			res.getEndDate()
		));
	}

	@DeleteMapping("/posts/{post-id}")
	public RsData<Void> deletePost(@PathVariable(name = "post-id") String postId) {
		adminService.deletePost(postId);
		return new RsData<>("204-1", "게시글 삭제 성공.");
	}

	// 최신 공지사항 5개 조회 엔드포인트 추가
	@GetMapping("/notices/latest")
	public RsData<List<NoticeResBody>> getLatestNotices() {
		List<NoticePost> latestNotices = adminService.getLatestNotices(5);
		List<NoticeResBody> res = latestNotices.stream()
				.map(NoticeResBody::of)
				.collect(Collectors.toList());
		return new RsData<>("200", "최신 공지사항 조회 성공.", res);
	}


}

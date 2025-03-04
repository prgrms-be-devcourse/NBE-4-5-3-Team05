package com.NBE_4_5_2.Team5.domain.admin.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.NBE_4_5_2.Team5.domain.admin.dto.NoticeResBody;
import com.NBE_4_5_2.Team5.domain.admin.service.AdminService;
import com.NBE_4_5_2.Team5.global.response.RsData;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

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
}

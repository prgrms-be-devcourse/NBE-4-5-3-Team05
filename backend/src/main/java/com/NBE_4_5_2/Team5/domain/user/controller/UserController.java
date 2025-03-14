package com.NBE_4_5_2.Team5.domain.user.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.NBE_4_5_2.Team5.domain.user.dto.AuthToken;
import com.NBE_4_5_2.Team5.domain.user.dto.SignUpUserForm;
import com.NBE_4_5_2.Team5.domain.user.dto.UserDto;
import com.NBE_4_5_2.Team5.domain.user.dto.UserUpdateRequest;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;
import com.NBE_4_5_2.Team5.global.Rq;
import com.NBE_4_5_2.Team5.global.dto.Empty;
import com.NBE_4_5_2.Team5.global.dto.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "사용자 API")
public class UserController {

	private final UserService userService;
	private final Rq rq;

	@PostMapping("/signup")
	@Operation(summary = "공지사항 등록", description = "새로운 공지사항을 등록합니다.")
	public RsData<UserDto> createUser(@RequestBody @Valid SignUpUserForm userForm) {

		User user = userService.createUser(userForm.username(), userForm.password(), userForm.email(),
			userForm.nickname(), userForm.address(), userForm.profileUrl());

		return new RsData<>("201-1", "회원 가입이 완료되었습니다.", new UserDto(user));
	}

	record LoginUserForm(
		@Parameter(description = "로그인 아이디")
		@NotBlank(message = "아이디는 필수 입력값입니다.") String username,
		@Parameter(description = "로그인 비밀번호")
		@NotBlank(message = "비밀번호는 필수 입력값입니다.") String password
	) {
	}

	record LoginUserDto(String accessToken, String refreshToken, UserDto item) {
	}

	@Operation(summary = "유저 로그인", description = "유저가 아이디와 비밀번호로 로그인합니다.")
	@PostMapping("/login")
	public RsData<LoginUserDto> loginUser(@RequestBody @Valid LoginUserForm userForm) {

		User user = userService.loginUser(userForm.username(), userForm.password());

		AuthToken authToken = userService.generateAuthtoken(user);
		rq.addCookie("accessToken", authToken.accessToken());
		rq.addCookie("refreshToken", authToken.refreshToken());

		return new RsData<>("200-1", "%s님 환영합니다.".formatted(user.getNickname()),
			new LoginUserDto(authToken.accessToken(), authToken.refreshToken(), new UserDto(user)));
	}

	@Operation(summary = "유저 로그아웃", description = "로그인한 유저가 로그아웃합니다.")
	@SecurityRequirement(name = "cookieAuth")
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/logout")
	public RsData<Void> logoutUser() {

		User userIdentity = rq.getUserIdentity();
		userService.logoutUser(userIdentity);

		rq.removeCookie("accessToken");
		rq.removeCookie("refreshToken");

		return new RsData<>("200-1", "로그아웃 되었습니다.");
	}

	//내 정보 조회
	@Operation(summary = "유저 정보 조회", description = "로그인한 유저의 정보를 조회합니다.")
	@SecurityRequirement(name = "cookieAuth")
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/me")
	public RsData<UserDto> me() {

		User userIdentity = rq.getUserIdentity();
		User user = rq.getRealActor(userIdentity);

		return new RsData<>("200-1", "내 정보 조회가 완료되었습니다.", new UserDto(user));
	}

	record RefreshUserForm(@NotBlank(message = "refreshToken을 입력해주세요.") String refreshToken) {
	}

	@Operation(summary = "토큰 재발급", description = "로그인한 유저가 만료된 accessToken을 재발급합니다.")
	@PostMapping("/refresh")
	public RsData<String> refreshAccessToken(@RequestBody @Valid RefreshUserForm userForm) {

		String refreshToken = userForm.refreshToken();
		String newAccessToken = userService.refreshAccessToken(refreshToken);
		rq.addCookie("accessToken", newAccessToken);

		return new RsData<>("200-1", "AccessToken이 재발급되었습니다.", newAccessToken);
	}

	//  내 정보 수정
	@Operation(summary = "유저 정보 수정", description = "로그인한 유저가 자신의 정보를 수정합니다.")
	@SecurityRequirement(name = "cookieAuth")
	@PreAuthorize("isAuthenticated()")
	@PutMapping("/me")
	@Transactional
	public RsData<UserDto> updateMyProfile(@RequestBody @Valid UserUpdateRequest updateRequest) {
		User userIdentity = rq.getUserIdentity();
		User user = rq.getRealActor(userIdentity);
		UserDto updatedUser = userService.updateMyProfile(user, updateRequest); // `userId` 대신 객체 전달
		return new RsData<>("200", "사용자 정보가 성공적으로 수정되었습니다.", updatedUser);
	}

	// 회원 탈퇴
	@Operation(summary = "유저 탈퇴", description = "로그인한 유저가 회원 탈퇴합니다.")
	@SecurityRequirement(name = "cookieAuth")
	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/me")
	public RsData<?> deleteMyProfile() {
		User userIdentity = rq.getUserIdentity();
		User user = rq.getRealActor(userIdentity);
		userService.deleteMyProfile(user);

		rq.removeCookie("accessToken");
		rq.removeCookie("refreshToken");

		return new RsData<>("200", "회원 탈퇴 성공", new Empty());
	}

}

package com.NBE_4_5_2.Team5.domain.user.user.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.NBE_4_5_2.Team5.domain.user.user.dto.AuthToken;
import com.NBE_4_5_2.Team5.domain.user.user.dto.SignUpUserForm;
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserDto;
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserUpdateRequest;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserAuthService;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService;
import com.NBE_4_5_2.Team5.global.Rq;
import com.NBE_4_5_2.Team5.global.dto.Empty;
import com.NBE_4_5_2.Team5.global.dto.RsData;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final Rq rq;
	private final UserAuthService userAuthService;

	@PostMapping("/signup")
	public RsData<UserDto> createUser(@RequestBody @Valid SignUpUserForm userForm) {

		User user = userService.createUser(userForm.username(), userForm.password(), userForm.email(),
			userForm.nickname(), userForm.address(), userForm.profileUrl());

		return new RsData<>("201-1", "회원 가입이 완료되었습니다.", new UserDto(user));
	}

	public record LoginUserForm(
		@NotBlank(message = "아이디는 필수 입력값입니다.") String username,
		@NotBlank(message = "비밀번호는 필수 입력값입니다.") String password
	) {
	}

	public record LoginUserDto(String accessToken, String refreshToken, UserDto item) {
	}

	@PostMapping("/login")
	public RsData<LoginUserDto> loginUser(@RequestBody @Valid LoginUserForm userForm) {

		User user = userService.loginUser(userForm.username(), userForm.password());

		AuthToken authToken = userService.generateAuthtoken(user);
		rq.addCookie("accessToken", authToken.accessToken());
		rq.addCookie("refreshToken", authToken.refreshToken());

		return new RsData<>("200-1", "%s님 환영합니다.".formatted(user.getNickname()),
			new LoginUserDto(authToken.accessToken(), authToken.refreshToken(), new UserDto(user)));
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/logout")
	public RsData<Void> logoutUser() {

		User userIdentity = userAuthService.getUserIdentity();
		userService.logoutUser(userIdentity);

		rq.removeCookie("accessToken");
		rq.removeCookie("refreshToken");

		return new RsData<>("200-1", "로그아웃 되었습니다.");
	}

	//내 정보 조회
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/me")
	public RsData<UserDto> me() {

		User userIdentity = userAuthService.getUserIdentity();
		User user = userAuthService.getRealActor(userIdentity);

		return new RsData<>("200-1", "내 정보 조회가 완료되었습니다.", new UserDto(user));
	}

	record RefreshUserForm(@NotBlank(message = "refreshToken을 입력해주세요.") String refreshToken) {
	}

	@PostMapping("/refresh")
	public RsData<String> refreshAccessToken(@RequestBody @Valid RefreshUserForm userForm) {

		String refreshToken = userForm.refreshToken();
		String newAccessToken = userService.refreshAccessToken(refreshToken);
		rq.addCookie("accessToken", newAccessToken);

		return new RsData<>("200-1", "AccessToken이 재발급되었습니다.", newAccessToken);
	}

	//  내 정보 수정
	@PreAuthorize("isAuthenticated()")
	@PutMapping("/me")
	public RsData<UserDto> updateMyProfile(@RequestBody @Valid UserUpdateRequest updateRequest) {
		User userIdentity = userAuthService.getUserIdentity();
		User user = userAuthService.getRealActor(userIdentity);
		UserDto updatedUser = userService.updateMyProfile(user, updateRequest); // `userId` 대신 객체 전달
		return new RsData<>("200", "사용자 정보가 성공적으로 수정되었습니다.", updatedUser);
	}

	// 회원 탈퇴
	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/me")
	public RsData<?> deleteMyProfile() {
		User userIdentity = userAuthService.getUserIdentity();
		User user = userAuthService.getRealActor(userIdentity);
		userService.deleteMyProfile(user);

		rq.removeCookie("accessToken");
		rq.removeCookie("refreshToken");

		return new RsData<>("200", "회원 탈퇴 성공", new Empty());
	}

	record EmailCodeRequest(String email) {
	}

	//인증 코드 이메일 발송
	@PostMapping("/email/code")
	public RsData<Void> sendAuthenticationCode(@RequestBody EmailCodeRequest userForm) {
		userService.sendAuthenticationCode(userForm.email());
		return new RsData<>("200-1", "이메일이 발송되었습니다.");
	}

	record VerifyCodeRequest(String email, String code) {
	}

	// 인증 코드 검증
	@PostMapping("/email/code/verify")
	public RsData<Void> verifyAuthenticationCode(@RequestBody VerifyCodeRequest userForm) {

		String email = userForm.email();
		String code = userForm.code();
		userService.verifyAuthenticationCode(email, code);

		return new RsData<>("200-1", "이메일이 인증에 성공했습니다.");
	}
}

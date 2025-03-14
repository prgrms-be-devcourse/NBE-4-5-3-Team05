package com.NBE_4_5_2.Team5.domain.user.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

	@Parameter(description = "닉네임")
	@Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
	private String nickname;

	@Parameter(description = "주소")
	@Size(max = 255, message = "주소는 최대 255자까지 입력 가능합니다.")
	private String address;

	@Parameter(description = "프로필 사진")
	@Size(max = 255, message = "프로필 URL은 최대 255자까지 입력 가능합니다.")
	private String profileUrl;

	@Parameter(description = "이메일")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	private String email;
}

package com.NBE_4_5_2.Team5.domain.user.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpUserForm(
	@Parameter(description = "로그인에 사용할 아이디")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문과 숫자만 사용할 수 있습니다.")
	@Size(min = 4, max = 20, message = "아이디는 4~20자 사이여야 합니다.")
	String username,

	@Parameter(description = "로그인에 사용할 비밀번호")
	@Size(min = 8, max = 50, message = "비밀번호는 8~50자 사이여야 합니다.")
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
		message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
	String password,

	@Parameter(description = "사용자 이메일. 유일해야 함")
	@NotBlank(message = "이메일은 필수 입력값입니다.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	String email,

	@Parameter(description = "사용자 닉네임. 유일해야 함")
	@Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
	String nickname,

	@Parameter(description = "사용자 주소.")
	@Size(max = 255, message = "주소는 최대 255자까지 입력 가능합니다.")
	String address,

	@Parameter(description = "사용자 프로필 사진.")
	@Size(max = 255, message = "프로필 URL은 최대 255자까지 입력 가능합니다.")
	String profileUrl
) {
}

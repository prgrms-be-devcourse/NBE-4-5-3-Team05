package com.NBE_4_5_2.Team5.domain.user.user.controller

import com.NBE_4_5_2.Team5.domain.user.user.dto.SignUpUserReqBody
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserDto
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserUpdateRequest
import com.NBE_4_5_2.Team5.domain.user.user.service.UserAuthService
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.global.rq.Rq
import com.NBE_4_5_2.Team5.global.dto.Empty
import com.NBE_4_5_2.Team5.global.response.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
@Tag(name = "User API", description = "사용자 관련 API")
class UserController(
    private val userService: UserService,
    private val rq: Rq,
    private val userAuthService: UserAuthService
) {

    @Operation(summary = "회원 가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/signup")
    fun createUser(@RequestBody @Valid body: SignUpUserReqBody): RsData<UserDto> {
        val user = userService.createUser(
            body.username, body.password, body.email, body.nickname, body.address, body.profileUrl
        )

        return RsData("201-1", "회원 가입이 완료되었습니다.", UserDto(user))
    }


    @JvmRecord
    data class LoginUserReqBody(
        @field:NotBlank(message = "아이디는 필수 입력값입니다.") val username: String,
        @field:NotBlank(message = "비밀번호는 필수 입력값입니다.") val password: String
    )

    @JvmRecord
    data class LoginUserResBody(val accessToken: String, val refreshToken: String, val item: UserDto)

    @Operation(summary = "로그인", description = "사용자가 로그인합니다.")
    @PostMapping("/login")
    fun loginUser(@RequestBody @Valid body: LoginUserReqBody): RsData<LoginUserResBody> {
        val user = userService.loginUser(body.username, body.password)

        val authToken = userService.generateAuthtoken(user)
        rq.addCookie("accessToken", authToken.accessToken)
        rq.addCookie("refreshToken", authToken.refreshToken)

        return RsData(
            "200-1",
            "${user.nickname}님 환영합니다.",
            LoginUserResBody(authToken.accessToken, authToken.refreshToken, UserDto(user))
        )
    }

    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    fun logoutUser(): RsData<Empty> {
        val userIdentity = userAuthService.userIdentity
        userService.logoutUser(userIdentity)

        rq.removeCookie("accessToken")
        rq.removeCookie("refreshToken")

        return RsData("200-1", "로그아웃 되었습니다.")
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인된 사용자의 정보를 조회합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    fun me(): RsData<UserDto> {
        val userIdentity = userAuthService.userIdentity
        val user = userAuthService.getRealActor(userIdentity)

        return RsData("200-1", "내 정보 조회가 완료되었습니다.", UserDto(user))
    }


    @JvmRecord
    data class RefreshUserReqBody(
        @field:NotBlank(message = "refreshToken을 입력해주세요.") val refreshToken: String
    )

    @Operation(summary = "AccessToken 재발급", description = "RefreshToken을 이용하여 새로운 AccessToken을 발급받습니다.")
    @PostMapping("/refresh")
    fun refreshAccessToken(@RequestBody @Valid body: RefreshUserReqBody): RsData<String> {
        val refreshToken = body.refreshToken
        val newAccessToken = userService.refreshAccessToken(refreshToken)
        rq.addCookie("accessToken", newAccessToken)

        return RsData("200-1", "AccessToken이 재발급되었습니다.", newAccessToken)
    }


    @Operation(summary = "내 정보 수정", description = "현재 로그인된 사용자의 정보를 수정합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me")
    fun updateMyProfile(@RequestBody @Valid updateRequest: UserUpdateRequest): RsData<UserDto> {
        val userIdentity = userAuthService.userIdentity
        val user = userAuthService.getRealActor(userIdentity)
        val updatedUser = userService.updateMyProfile(user, updateRequest) // `userId` 대신 객체 전달
        return RsData("200", "사용자 정보가 성공적으로 수정되었습니다.", updatedUser)
    }


    @Operation(summary = "회원 탈퇴", description = "현재 로그인된 사용자의 계정을 삭제합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/me")
    fun deleteMyProfile(): RsData<Empty> {
        val userIdentity = userAuthService.userIdentity
        val user = userAuthService.getRealActor(userIdentity)
        userService.deleteMyProfile(user)

        rq.removeCookie("accessToken")
        rq.removeCookie("refreshToken")

        return RsData("200", "회원 탈퇴 성공")
    }


    @JvmRecord
    data class EmailUserReqBody(
        @field:Email(message = "올바른 이메일 형식이 아닙니다.") val email: String
    )

    @Operation(summary = "이메일 인증 코드 발송", description = "사용자의 이메일로 인증 코드를 발송합니다.")
    @PostMapping("/email/code")
    fun sendAuthenticationCode(@RequestBody @Valid body: EmailUserReqBody): RsData<Void> {
        val email = body.email
        userService.sendAuthenticationCode(email)

        return RsData("200-1", "이메일이 발송되었습니다.")
    }

    @JvmRecord
    data class VerifyCodeUserReqBody(val email: String, val code: String)

    @Operation(summary = "이메일 인증 코드 검증", description = "사용자가 입력한 인증 코드를 검증합니다.")
    @PostMapping("/email/code/verify")
    fun verifyAuthenticationCode(@RequestBody body: VerifyCodeUserReqBody): RsData<Empty> {
        val email = body.email
        val code = body.code
        userService.verifyAuthenticationCode(email, code)

        return RsData("200-1", "이메일이 인증에 성공했습니다.")
    }
}

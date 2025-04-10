package com.NBE_4_5_2.Team5.domain.user.admin.controller

import com.NBE_4_5_2.Team5.domain.user.admin.dto.BanResBody
import com.NBE_4_5_2.Team5.domain.user.admin.dto.NoticeResBody
import com.NBE_4_5_2.Team5.domain.user.admin.entity.NoticePost
import com.NBE_4_5_2.Team5.domain.user.admin.service.AdminService
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserDto
import com.NBE_4_5_2.Team5.global.response.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.stream.Collectors

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Tag(name = "Admin API", description = "관리자 전용 API")
class AdminController(
    private val adminService: AdminService
) {


    data class NoticeReqBody(
        @Parameter(
            description = "공지사항 제목",
            example = "서비스 점검 안내"
        ) @Parameter(
            description = "공지사항 제목",
            example = "서비스 점검 안내"
        ) val title: @NotEmpty String,
        @Parameter(
            description = "공지사항 내용",
            example = "서비스 점검으로 인해 3월 15일 02시부터 04시까지 이용이 제한됩니다."
        ) @Parameter(
            description = "공지사항 내용",
            example = "서비스 점검으로 인해 3월 15일 02시부터 04시까지 이용이 제한됩니다."
        ) val content: @NotEmpty String
    )


    @Operation(summary = "공지사항 등록", description = "새로운 공지사항을 등록합니다.")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "공지사항 등록 성공")])
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated")
    @PostMapping("/notices")
    fun writeNotice(
        @Parameter(description = "공지사항 등록 body") @RequestBody body: @Valid NoticeReqBody
    ): RsData<NoticeResBody> {
        val data = adminService.writeNotice(body.title, body.content)

        return RsData("200-1", "공지사항 등록 성공.", data)
    }


    data class BanReqBody(val reason: @NotEmpty String)

    @Operation(summary = "유저 정지", description = "특정 유저를 정지시킵니다.")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "유저 정지 성공")])
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated")
    @PostMapping("/users/{user-id}/ban")
    fun banUser(
        @Parameter(description = "유저 id") @PathVariable(name = "user-id") userId: String,
        @Parameter(description = "공지사항 등록 요청 바디") @RequestBody reason: @Valid BanReqBody
    ): RsData<BanResBody> {
        val res = adminService.banUser(userId, reason.reason)
        return RsData(
            "200-1", "유저 정지 성공",
            BanResBody(
                res.id, userId, reason.reason, res.user.blockedCount, res.startDate,
                res.endDate
            )
        )
    }


    @Operation(summary = "계정 정지 해제", description = "특정 유저의 정지를 해제합니다.")
    @ApiResponses(value = [ApiResponse(responseCode = "204", description = "계정 정지 해제 성공.")])
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated")
    @DeleteMapping("/users/{user-id}/ban")
    fun unBanUser(@PathVariable(name = "user-id") userId: String): RsData<Void> {
        adminService.unBanUser(userId)
        return RsData("204-1", "계정 정지 해제 성공.")
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @ApiResponses(value = [ApiResponse(responseCode = "204", description = "게시글 삭제 성공")])
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/posts/{post-id}")
    fun deletePost(
        @Parameter(description = "post id") @PathVariable(name = "post-id") postId: String
    ): RsData<Void> {
        adminService.deletePost(postId)
        return RsData("204-1", "게시글 삭제 성공.")
    }

    @GetMapping("/notices/latest")
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "최신 공지사항 조회 성공"
        )]
    )
    @Operation(summary = "최신 공지사항 조회", description = "최신 공지사항 5개를 조회합니다.")
    fun getLatestNotices(): RsData<List<NoticeResBody>> {

        // 최신 공지사항 5개 조회 엔드포인트 추가 get() {
        val latestNotices: List<NoticePost> = adminService.getLatestNotices(5)
        val res: List<NoticeResBody> = latestNotices.stream()
            .map { obj: NoticePost -> NoticeResBody.of(obj) }
            .collect(Collectors.toList())
        return RsData<List<NoticeResBody>>(
            "200",
            "최신 공지사항 조회 성공.",
            res
        )
    }

    @Operation(summary = "유저 리스트 조회", description = "등록된 유저 리스트를 조회합니다.")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "유저 리스트 조회 성공")])
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/users")
    fun getUserList(@PageableDefault(size = 10, page = 0) pageable: Pageable): RsData<Page<UserDto>> {
        val users: Page<UserDto> = adminService.getUsers(pageable)

        return RsData("200-1", "유저 리스트 조회 성공.", users)
    }

    @Operation(summary = "공지사항 리스트 조회", description = "공지사항 리스트를 조회합니다.")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "공지사항 리스트 조회 성공")])
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/notices")
    fun getNotices(@PageableDefault(size = 10, page = 0) pageable: Pageable): RsData<Page<NoticeResBody>> {
        val notices: Page<NoticeResBody> = adminService.getNotices(pageable)
        return RsData("200-1", "공지사항 리스트 조회 성공.", notices)
    }


    data class UpdateNoticeReq(val title: String, val content: String)

    @Operation(summary = "공지사항 수정", description = "기존 공지사항을 수정합니다.")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "공지사항 업데이트 성공")])
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/notices/{notice-id}")
    fun updateNotice(
        @PathVariable(name = "notice-id") noticeId: String,
        @RequestBody body: UpdateNoticeReq
    ): RsData<NoticeResBody> {
        val res: NoticeResBody = adminService.updateNotice(noticeId, body)
        return RsData("200-1", "공지사항 업데이트 성공.", res)
    }

    @Operation(summary = "공지사항 삭제", description = "기존 공지사항을 삭제합니다.")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "공지사항 삭제 성공")])
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/notices/{notice-id}")
    fun deleteNotice(@PathVariable(name = "notice-id") noticeId: String): RsData<Void> {
        adminService.deleteNotice(noticeId)
        return RsData("200-1", "공지사항 삭제 완료.")
    }

    @Operation(summary = "공지사항 조회", description = "공지사항을 조회합니다.")
    @GetMapping("/notices/{notice-id}")
    fun getNotice(@PathVariable(name = "notice-id") noticeId: String): RsData<NoticeResBody> {
        val noticeDto: NoticeResBody = adminService.getNotice(noticeId)

        return RsData("200-1", "공지사항 조회 성공.", noticeDto)
    }

    data class SignUpAdminReqBody(
        @field:Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문과 숫자만 사용할 수 있습니다.")
        @field:Size(min = 4, max = 20, message = "아이디는 4~20자 사이여야 합니다.")
        val username:  String,
        val password:  String,
        @field:Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
        val nickname:  String,
        @field:NotBlank(message = "이메일은 필수 입력값입니다.")
        @field:Email(message = "올바른 이메일 형식이 아닙니다.")
        val email: String
    )

    @Operation(summary = "관리자 회원가입", description = "superadmin 권한으로 새로운 admin 계정을 생성합니다.")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "관리자 회원가입 성공")])
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/signup")
    fun signUpAdmin(@RequestBody @Valid body: SignUpAdminReqBody): RsData<UserDto> {

        val newAdmin = adminService.signUpAdmin(
            body.username,
            body.password,
            body.nickname,
            body.email
        )
        return RsData("200-1", "관리자 회원가입 성공.", UserDto(newAdmin))
    }

    @Operation(summary = "관리자 삭제", description = "superadmin 권한으로 특정 admin 계정을 삭제합니다.")
    @ApiResponses(value = [ApiResponse(responseCode = "204", description = "관리자 삭제 성공")])
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{adminId}")
    fun deleteAdmin(@PathVariable adminId: String): RsData<Void> {

        adminService.deleteAdmin(adminId)
        return RsData("200-1", "관리자 삭제 성공.")
    }

    @Operation(summary = "관리자 리스트 조회", description = "등록된 관리자 리스트를 조회합니다.")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "관리자 리스트 조회 성공")])
    @SecurityRequirement(name = "cookieAuth")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/admins")
    fun getAdminList(@PageableDefault(size = 10, page = 0) pageable: Pageable): RsData<Page<UserDto>> {
        val admins: Page<UserDto> = adminService.getAdmins(pageable)
        return RsData("200-1", "관리자 리스트 조회 성공.", admins)
    }
}

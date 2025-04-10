package com.NBE_4_5_2.Team5.domain.user.admin.service

import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository
import com.NBE_4_5_2.Team5.domain.user.admin.controller.AdminController.UpdateNoticeReq
import com.NBE_4_5_2.Team5.domain.user.admin.dto.BanListDto
import com.NBE_4_5_2.Team5.domain.user.admin.dto.NoticeResBody
import com.NBE_4_5_2.Team5.domain.user.admin.entity.BanList
import com.NBE_4_5_2.Team5.domain.user.admin.entity.NoticePost
import com.NBE_4_5_2.Team5.domain.user.admin.repository.BanListRepository
import com.NBE_4_5_2.Team5.domain.user.admin.repository.NoticePostRepository
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserDto
import com.NBE_4_5_2.Team5.domain.user.user.entity.Role
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.repository.UserRepository
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.domain.user.user.service.UserValidator
import com.NBE_4_5_2.Team5.global.exception.notice.NoticeNotFoundException
import com.NBE_4_5_2.Team5.global.exception.security.WrongRoleException
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.constraints.NotEmpty
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.stream.Collectors

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
class AdminService(
    private val userValidator: UserValidator,
    private val banListRepository: BanListRepository,
    private val userRepository: UserRepository,
    private val noticePostRepository: NoticePostRepository,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val productPostRepository: ProductPostRepository,
) {

    companion object {
        private const val BAN_DURATION_WEIGHT = 7
    }

    @Transactional
    fun signUpSuperAdmin(username: String, password: String, nickname: String, email: String): User {
        return User(
            username,
            passwordEncoder.encode(password),
            email,
            nickname,
            "addr",
            "url",
            Role.SUPER_ADMIN
        )
            .let {
                userService.deleteAuthenticationCode(it.email)
                userRepository.save(it)
            }
    }

    @Transactional
    fun signUpAdmin(username: String, password: String, nickname: String, email: String): User {

        userService.checkDuplicateAndEmail(username, nickname, email)

        return User(
            username,
            passwordEncoder.encode(password),
            email,
            nickname,
            "addr",
            "url",
            Role.ADMIN
        )
            .let {
                userRepository.save(it)
            }
    }

    fun writeNotice(title: @NotEmpty String, content: @NotEmpty String): NoticeResBody {
        isAdmin(loggedInUser)

        return NoticePost(
            title,
            content,
            loggedInUser
        )
            .let {
                noticePostRepository.save(it)
            }.let {
                NoticeResBody.of(it)
            }

    }

    fun banUser(userId: String, reason: @NotEmpty String): BanListDto {

        isAdmin(loggedInUser)

        return userRepository.findById(userId)
            .orElseThrow { EntityNotFoundException("id가 ${userId}인 user를 찾을 수 없습니다.") }
            .let {

                it.ban()
                addNewBanList(reason, it)
            }.let {
                BanListDto(it)
            }
    }

    private fun addNewBanList(reason: String, bannedUser: User): BanList {
        return BanList(
            reason,
            bannedUser,
            LocalDateTime.now().plusDays((bannedUser.blockedCount + 1).toLong() * BAN_DURATION_WEIGHT)
        )
            .let {
                banListRepository.save(it)
            }
    }

    private val loggedInUser: User
        get() = userService.userIdentity

    private fun isAdmin(admin: User) {
        if(admin.role == Role.SUPER_ADMIN) return

        if (admin.role != Role.ADMIN) {
            throw WrongRoleException(HttpStatus.BAD_REQUEST.toString(), "관리자만 작성할 수 있는 글입니다.")
        }
    }

    fun deletePost(postId: String) {
        isAdmin(loggedInUser)

        productPostRepository.deleteById(postId)
    }

    fun getUsers(pageable: Pageable): Page<UserDto> {
        val all = userRepository.findAll(pageable)
        return all.map { admin: User -> UserDto(admin) }
    }

    fun unBanUser(userId: String) {

        isAdmin(loggedInUser)

        userRepository.findById(userId)
            .orElseThrow { UsernameNotFoundException("유저를 찾을 수 없습니다.") }
            .let {

                check(it.blocked) { "계정 정지 상태가 아닙니다." }

                it.unBan()
                removeBanInfo(userId)
            }

    }

    /**
     * `userId`를 가진 유저의 밴 이력을 삭제한다.
     *
     * @param userId
     */
    private fun removeBanInfo(userId: String) {
        banListRepository.deleteBy_bannedUser_Id(userId)
    }

    fun getNotices(pageable: Pageable): Page<NoticeResBody> {
        isAdmin(loggedInUser)

        val all = noticePostRepository.findAll(pageable)
        return all.map { notice: NoticePost -> NoticeResBody.of(notice) }
    }

    fun updateNotice(noticeId: String, body: UpdateNoticeReq): NoticeResBody {
        isAdmin(loggedInUser)

        val noticePost = noticePostRepository.findById(noticeId)
            .orElseThrow { NoticeNotFoundException("404-1", "Notice post를 찾을 수 없습니다.") }

        return noticePost.update(body.title, body.content)
            .let {
                NoticeResBody.of(noticePost)
            }
    }

    fun deleteNotice(noticeId: String) {
        isAdmin(loggedInUser)
        noticePostRepository.deleteById(noticeId)
    }

    // 최신 공지사항을 조회하는 메서드 (최신순 정렬 후 상위 limit 개 반환)
    @Transactional(readOnly = true)
    fun getLatestNotices(limit: Int): List<NoticePost> {
        val notices = noticePostRepository.findAll()
        return notices.stream()
            .sorted(Comparator.comparing(NoticePost::createdDate).reversed())
            .limit(limit.toLong())
            .collect(Collectors.toList())
    }

    fun getNotice(noticeId: String): NoticeResBody {
        return noticePostRepository.findById(noticeId)
            .orElseThrow { NoticeNotFoundException() }
            .let {
                NoticeResBody.of(it)
            }
    }


}

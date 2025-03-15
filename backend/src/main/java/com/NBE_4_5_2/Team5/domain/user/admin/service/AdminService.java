package com.NBE_4_5_2.Team5.domain.user.admin.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository;
import com.NBE_4_5_2.Team5.domain.user.admin.controller.AdminController;
import com.NBE_4_5_2.Team5.domain.user.admin.dto.BanListDto;
import com.NBE_4_5_2.Team5.domain.user.admin.dto.NoticeResBody;
import com.NBE_4_5_2.Team5.domain.user.admin.entity.BanList;
import com.NBE_4_5_2.Team5.domain.user.admin.entity.NoticePost;
import com.NBE_4_5_2.Team5.domain.user.admin.repository.BanListRepository;
import com.NBE_4_5_2.Team5.domain.user.admin.repository.NoticePostRepository;
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserDto;
import com.NBE_4_5_2.Team5.domain.user.user.entity.Role;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.repository.UserRepository;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService;
import com.NBE_4_5_2.Team5.global.exception.notice.NoticeNotFoundException;
import com.NBE_4_5_2.Team5.global.exception.security.WrongRoleException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {
	private static final int BAN_DURATION_WEIGHT = 7;
	private final BanListRepository banListRepository;
	private final UserRepository userRepository;
	private final NoticePostRepository noticePostRepository;
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final ProductPostRepository productPostRepository;

	public User signUpAdmin(String username, String password, String nickname, String email) {
		User admin =
			User.builder()
				.id("user-" + UUID.randomUUID())
				.role(Role.ADMIN)
				.username(username)
				.password(passwordEncoder.encode(password))
				.email(email)
				.nickname(nickname)
				.address("addr")
				.profileUrl("url")
				.build();

		/**
		 *  기존 코드
		 * 	admin.setRefreshToken(UUID.randomUUID().toString());
		 *
		 * refreshToken을 login 할 때 발급하여 redis에 저장하는 방식으로 변경했습니다.
		 * 이에 따라 기존에 setRefreshToken 하던 방식에서 generateAuthtoken 하여 redis에 저장하는 방식으로
		 * 변경했습니다.
		 * */
		userService.generateAuthtoken(admin);

		return userRepository.save(admin);
	}

	public NoticeResBody writeNotice(@NotEmpty String title, @NotEmpty String content) {
		User admin = getUser();

		isAdmin(admin);

		NoticePost noticePost = NoticePost.builder().title(title).content(content)
			.admin(admin).build();

		NoticePost saved = noticePostRepository.save(noticePost);

		return NoticeResBody.of(saved);

	}

	public BanListDto banUser(String userId, @NotEmpty String reason) {
		User loggedInUser = getUser();
		User bannedUser = userRepository.findById(userId)
			.orElseThrow(() -> new EntityNotFoundException("id가 %s인 user를 찾을 수 없습니다.".formatted(userId)));

		isAdmin(loggedInUser);

		BanList saved = addNewBanList(reason, bannedUser);

		bannedUser.ban();

		return new BanListDto(saved);
	}

	private BanList addNewBanList(String reason, User bannedUser) {
		BanList banList = new BanList(reason, bannedUser, LocalDateTime.now()
			.plusDays((long)(bannedUser.getBlockedCount() + 1) * BAN_DURATION_WEIGHT));

		return banListRepository.save(banList);
	}

	private User getUser() {
		return userService.getUserIdentity();
	}

	private void isAdmin(User admin) {
		if (!admin.getRole().equals(Role.ADMIN)) {
			throw new WrongRoleException(HttpStatus.BAD_REQUEST.toString(), "관리자만 작성할 수 있는 글입니다.");
		}
	}

	public void deletePost(String postId) {
		User loggedInAdmin = getUser();

		isAdmin(loggedInAdmin);

		productPostRepository.deleteById(postId);
	}

	public Page<UserDto> getUsers(Pageable pageable) {
		Page<User> all = userRepository.findAll(pageable);
		return all.map(UserDto::new);
	}

	public void unBanUser(String userId) {
		User loggedInUser = getUser();

		isAdmin(loggedInUser);

		User unBanUser = userRepository.findById(userId)
			.orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

		if (!unBanUser.getBlocked()) {
			throw new IllegalStateException("계정 정지 상태가 아닙니다.");
		}

		unBanUser.unBan();

		removeBanInfo(userId);
	}

	/**
	 * {@code userId}를 가진 유저의 밴 이력을 삭제한다.
	 * @param userId
	 */
	private void removeBanInfo(String userId) {
		banListRepository.deleteByBannedUser_Id(userId);
	}

	public Page<NoticeResBody> getNotices(Pageable pageable) {
		User loggedInAdmin = getUser();
		isAdmin(loggedInAdmin);

		Page<NoticePost> all = noticePostRepository.findAll(pageable);
		return all.map(notice -> NoticeResBody.of(notice));
	}

	public NoticeResBody updateNotice(String noticeId, AdminController.UpdateNoticeReq body) {
		User admin = getUser();

		isAdmin(admin);

		NoticePost noticePost = noticePostRepository.findById(noticeId)
			.orElseThrow(() -> new NoticeNotFoundException("404-1", "Notice post를 찾을 수 없습니다."));

		return NoticeResBody.of(noticePost.update(body.title(), body.content()));
	}

	public void deleteNotice(String noticeId) {
		User user = getUser();
		isAdmin(user);
		noticePostRepository.deleteById(noticeId);
	}

	// 최신 공지사항을 조회하는 메서드 (최신순 정렬 후 상위 limit 개 반환)
	@Transactional(readOnly = true)
	public List<NoticePost> getLatestNotices(int limit) {
		List<NoticePost> notices = noticePostRepository.findAll();
		return notices.stream()
			.sorted(Comparator.comparing(NoticePost::getCreatedAt).reversed())
			.limit(limit)
			.collect(Collectors.toList());
	}

}

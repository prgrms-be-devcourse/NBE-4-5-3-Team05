package com.NBE_4_5_2.Team5.domain.admin.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.NBE_4_5_2.Team5.domain.admin.dto.NoticeResBody;
import com.NBE_4_5_2.Team5.domain.admin.entity.NoticePost;
import com.NBE_4_5_2.Team5.domain.admin.repository.NoticePostRepository;
import com.NBE_4_5_2.Team5.domain.user.entity.Role;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.repository.UserRepository;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {
	private final UserRepository userRepository;
	private final NoticePostRepository noticePostRepository;
	private final PasswordEncoder passwordEncoder;

	public User signUpAdmin(String username, String password, String email) {
		User admin = new User(username, passwordEncoder.encode(password), email, "admin", "addr", "profile",
			Role.ADMIN);
		admin.setRefreshToken(UUID.randomUUID().toString());

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

	private void isAdmin(User admin) {
		if (!admin.getRole().equals(Role.ADMIN)) {
			throw new ServiceException(HttpStatus.BAD_REQUEST.toString(), "관리자만 작성할 수 있는 글입니다.");
		}
	}

	private User getUser() {
		return userRepository.findAllByRole(Role.ADMIN).get(0);
	}
}

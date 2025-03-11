package com.NBE_4_5_2.Team5.domain.user.user.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.NBE_4_5_2.Team5.domain.user.user.dto.AuthToken;
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserDto;
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserUpdateRequest;
import com.NBE_4_5_2.Team5.domain.user.user.entity.RefreshToken;
import com.NBE_4_5_2.Team5.domain.user.user.entity.Role;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.repository.UserRepository;
import com.NBE_4_5_2.Team5.global.Rq;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;
import com.NBE_4_5_2.Team5.global.exception.security.AuthenticationNotFoundException;
import com.NBE_4_5_2.Team5.global.exception.security.AuthenticationNotValidException;
import com.NBE_4_5_2.Team5.global.exception.security.TokenNotFoundException;
import com.NBE_4_5_2.Team5.global.exception.security.TokenNotValidException;
import com.NBE_4_5_2.Team5.global.exception.validation.AlreadyUsedException;
import com.NBE_4_5_2.Team5.global.security.SecurityUser;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final AuthTokenService authTokenService;
	private final RedisService redisService;
	private final PasswordEncoder passwordEncoder;
	private final UserValidator userValidator;
	private final Rq rq;

	public User createUser(String username, String password, String email,
		String nickname, String address, String profileUrl) {

		userValidator.duplicate(username, email, nickname);

		User user = User.builder()
			.id("user-" + UUID.randomUUID())
			.username(username)
			.password(passwordEncoder.encode(password))
			.email(email)
			.nickname(nickname)
			.address(address)
			.profileUrl(profileUrl)
			.role(Role.USER)
			.build();

		return userRepository.save(user);
	}

	/**
	 * 로그인 검증
	 *
	 * @param username 사용자 아이디
	 * @param password 사용자 비밀번호
	 * @return 검증된 User 객체
	 */
	public User loginUser(String username, String password) {
		return userValidator.credentials(username, password);
	}

	/**
	 * 로그아웃 처리 (redis에서 refreshToken 제거)
	 * <p>
	 * redis에 저장된 refreshToken을 제거합니다.
	 * 1. 로그인된 authentication의 UserId를 기반으로 삭제합니다.
	 * 2. 삭제 실패 시 사용자가 보유한 refreshToken을 기반으로 다시 삭제합니다.
	 */
	public void logoutUser(User userIdentity) {
		boolean isDeleted = redisService.deleteTokenByUserId(userIdentity.getId());

		if (!isDeleted) {
			rq.getRefreshToken().ifPresent(redisService::deleteTokenByRefreshToken);
		}
	}

	public Optional<User> getUserById(String id) {
		return userRepository.findById(id);
	}

	public Optional<User> getUserByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	/**
	 * AccessToken payload에 저장된 id와 username, role만을 가진 User 객체를 반환
	 */
	public Optional<User> getUserByAccessToken(String accessToken) {

		Map<String, Object> payload = authTokenService.getPayload(accessToken);

		if (payload == null) {
			return Optional.empty();
		}

		String id = (String)payload.get("id");
		String username = (String)payload.get("username");
		Role role = (Role)payload.get("role");

		return Optional.of(
			User.builder()
				.id(id)
				.username(username)
				.role(role)
				.build()
		);
	}

	/**
	 * Redis에 refreshToken 저장
	 *
	 * @param user         로그인한 사용자
	 * @param refreshToken 저장할 refreshToken
	 *                     기존에 userId로 저장된 refreshToken이 존재할 경우 덮어 씌웁니다.
	 */
	public void saveRefreshToken(User user, String refreshToken) {
		redisService.createToken(user, refreshToken);
	}

	/**
	 * refreshToken 검증
	 *
	 * @param user         로그인한 사용자
	 * @param refreshToken 검증할 refreshToken
	 * @throws ServiceException 사용자의 userId로 된 refreshToken이 존재하지 않거나 값이 일치하지 않을 경우
	 */
	public void validateRefreshToken(User user, String refreshToken) {
		String userId = user.getId();

		String storedRefreshToken = redisService.getTokenByUserId(userId)
			.map(RefreshToken::getRefreshToken)
			.orElseThrow(() -> new TokenNotFoundException("401-1", "로그인이 필요합니다."));

		if (!storedRefreshToken.equals(refreshToken)) {
			throw new TokenNotValidException("401-2", "유효하지 않은 RefreshToken입니다.");
		}
	}

	/**
	 * User 정보로 AuthToken을 생성하여 반환
	 * refreshToken은 redis에 저장됨
	 *
	 * @param user 로그인한 사용자
	 * @return refreshToken, accessToken을 담은 AuthToken 객체
	 */
	public AuthToken generateAuthtoken(User user) {
		String refreshToken = authTokenService.generateRefreshToken();
		String accessToken = authTokenService.generateAccessToken(user);

		saveRefreshToken(user, refreshToken);
		return new AuthToken(refreshToken, accessToken);
	}

	/**
	 * User 정보로 AuthToken을 생성하여 String 형태로 반환
	 *
	 * @param user 로그인한 사용자
	 * @return refreshToken, accessToken을 공백으로 구분한 문자열
	 * refreshToken은 redis에 저장됨
	 */
	public String generateAuthTokenAsString(User user) {
		AuthToken authToken = generateAuthtoken(user);
		return authToken.refreshToken() + " " + authToken.accessToken();
	}

	public String getRefreshTokenByUserId(String userId) {
		return redisService.getTokenByUserId(userId)
			.map(RefreshToken::getRefreshToken)
			.orElseThrow(() -> new TokenNotFoundException("401-1", "로그인이 필요합니다."));
	}

	/**
	 * refreshToken을 기반으로 User 객체를 반환
	 *
	 * @param refreshToken 검증할 refreshToken
	 *                     redis에 존재하지 않을 경우 Optional.empty() 반환
	 * @return refreshToken을 기반으로 찾은 User 객체
	 */
	public Optional<User> getUserByRefreshToken(String refreshToken) {
		Optional<RefreshToken> tokenByRefreshToken = redisService.getTokenByRefreshToken(refreshToken);

		if (tokenByRefreshToken.isEmpty()) {
			return Optional.empty();
		}

		String userId = tokenByRefreshToken.get()
			.getUserId().substring("refreshToken:".length());

		return userRepository.findById(userId);
	}

	public long count() {
		return userRepository.count();
	}

	public User getUserIdentity() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			throw new AuthenticationNotFoundException("401-2", "로그인이 필요합니다.");
		}

		Object principal = authentication.getPrincipal();

		if (!(principal instanceof SecurityUser)) {
			throw new AuthenticationNotValidException("401-3", "잘못된 인증 정보입니다");
		}

		SecurityUser user = (SecurityUser)principal;

		return User.builder()
			.id(user.getId())
			.username(user.getUsername())
			.role(user.getRole())
			.build();
	}

	// 내 프로필 수정
	@Transactional
	public UserDto updateMyProfile(User user, UserUpdateRequest updateRequest) {
		// 닉네임 변경
		if (updateRequest.getNickname() != null) {
			user.setNickname(updateRequest.getNickname());
		}

		// 주소 변경
		if (updateRequest.getAddress() != null) {
			user.setAddress(updateRequest.getAddress());
		}

		// 프로필 이미지 변경
		if (updateRequest.getProfileUrl() != null) {
			user.setProfileUrl(updateRequest.getProfileUrl());
		}

		// 이메일 변경 시 중복 체크
		if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
			if (userRepository.existsByEmail(updateRequest.getEmail())) {
				throw new AlreadyUsedException("400-EMAIL-ALREADY-EXISTS", "이미 사용 중인 이메일입니다.");
			}
			user.setEmail(updateRequest.getEmail());
		}

		return UserDto.fromEntity(user);
	}

	// 회원 탈퇴
	@Transactional
	public void deleteMyProfile(User user) {
		userRepository.delete(user);
	}

}

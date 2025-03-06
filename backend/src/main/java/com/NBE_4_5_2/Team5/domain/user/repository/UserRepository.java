package com.NBE_4_5_2.Team5.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.NBE_4_5_2.Team5.domain.user.entity.Role;
import com.NBE_4_5_2.Team5.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, String> {
	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	Optional<User> findByNickname(String nickname);

	Optional<User> findByRefreshToken(String refreshToken);

	// 이메일 중복 체크
	boolean existsByEmail(String email);

	List<User> findAllByRole(Role role);
}

package com.NBE_4_5_2.Team5.domain.user.user.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.NBE_4_5_2.Team5.domain.user.user.entity.RefreshToken;

public interface RedisRepository extends CrudRepository<RefreshToken, String> {
	void deleteByRefreshToken(String refreshToken);

	Optional<RefreshToken> findByRefreshToken(String refreshToken);
}

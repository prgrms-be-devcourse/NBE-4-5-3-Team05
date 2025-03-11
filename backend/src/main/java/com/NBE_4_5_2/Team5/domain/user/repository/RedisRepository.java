package com.NBE_4_5_2.Team5.domain.user.repository;

import com.NBE_4_5_2.Team5.domain.user.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RedisRepository extends CrudRepository<RefreshToken, String> {
    void deleteByRefreshToken(String refreshToken);
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}

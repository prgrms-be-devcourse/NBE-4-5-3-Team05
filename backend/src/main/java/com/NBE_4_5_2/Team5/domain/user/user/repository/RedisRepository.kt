package com.NBE_4_5_2.Team5.domain.user.user.repository

import com.NBE_4_5_2.Team5.domain.user.user.entity.RefreshToken
import org.springframework.data.repository.CrudRepository

interface RedisRepository : CrudRepository<RefreshToken, String> {
    fun deleteByRefreshToken(refreshToken: String)
    fun findByRefreshToken(refreshToken: String): RefreshToken?
}

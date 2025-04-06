package com.NBE_4_5_2.Team5.domain.user.user.repository

import com.NBE_4_5_2.Team5.domain.user.user.entity.RefreshToken
import org.springframework.data.repository.CrudRepository
import java.util.*

interface RedisRepository : CrudRepository<RefreshToken, String> {
    fun deleteByRefreshToken(refreshToken: String)
    fun findByRefreshToken(refreshToken: String): Optional<RefreshToken>
}

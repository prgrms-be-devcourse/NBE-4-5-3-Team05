package com.NBE_4_5_2.Team5.domain.user.admin.repository

import com.NBE_4_5_2.Team5.domain.user.admin.entity.BanList
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface BanListRepository : JpaRepository<BanList, String> {
    fun findAllBy_endDateIsAfter(endDateAfter: LocalDateTime): List<BanList>

    fun deleteBy_bannedUser_Id(bannedUserId: String)
}
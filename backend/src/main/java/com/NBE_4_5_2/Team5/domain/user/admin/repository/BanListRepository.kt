package com.NBE_4_5_2.Team5.domain.user.admin.repository

import com.NBE_4_5_2.Team5.domain.user.admin.entity.BanList
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface BanListRepository : JpaRepository<BanList?, String?> {

     fun deleteBy_bannedUser_Id(userId: String)
     fun findAllBy_endDateIsAfter(_endDateAfter: LocalDateTime): MutableList<BanList>
}
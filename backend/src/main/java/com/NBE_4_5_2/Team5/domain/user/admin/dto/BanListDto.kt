package com.NBE_4_5_2.Team5.domain.user.admin.dto

import com.NBE_4_5_2.Team5.domain.user.admin.entity.BanList
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserDto
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import java.time.LocalDateTime

@NoArgsConstructor
@Getter
@AllArgsConstructor
class BanListDto(
    var id: String,
    var reason: String,
    var user: UserDto,
    var startDate: LocalDateTime,
    var endDate: LocalDateTime,
) {
    constructor(saved: BanList) : this(saved.id, saved.reason, UserDto(saved.bannedUser), saved.startDate, saved.endDate)
}

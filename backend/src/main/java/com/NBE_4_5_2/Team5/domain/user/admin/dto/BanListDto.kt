package com.NBE_4_5_2.Team5.domain.user.admin.dto

import com.NBE_4_5_2.Team5.domain.user.admin.entity.BanList
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserDto
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor

@NoArgsConstructor
@Getter
@AllArgsConstructor
class BanListDto(saved: BanList) {
    var id = saved.id
    var reason = saved.reason
    var user = UserDto(saved.bannedUser)
    var startDate = saved.startDate
    var endDate = saved.endDate
}

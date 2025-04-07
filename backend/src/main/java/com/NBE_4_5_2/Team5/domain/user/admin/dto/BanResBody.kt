package com.NBE_4_5_2.Team5.domain.user.admin.dto

import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import java.time.LocalDateTime

class BanResBody(
    var banListId: String,
    var userId: String,
    var reason: String,
    var banCount: Int,
    var startDate: LocalDateTime,
    var endDate: LocalDateTime
) {

}

package com.NBE_4_5_2.Team5.batch

import com.NBE_4_5_2.Team5.domain.user.admin.entity.BanList
import com.NBE_4_5_2.Team5.domain.user.admin.repository.BanListRepository
import lombok.RequiredArgsConstructor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.function.Consumer

@Component
@RequiredArgsConstructor
class BanListBatchScheduler {
    private val banListRepository: BanListRepository? = null

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    fun schedule() {
        val allByEndDateAfter = banListRepository!!.findAllBy_endDateIsAfter(LocalDate.now().atStartOfDay())
        allByEndDateAfter.forEach(Consumer { item: BanList -> item.bannedUser.unBan() })
    }
}

package com.NBE_4_5_2.Team5.domain.user.user.service.email.service

import com.NBE_4_5_2.Team5.global.config.email.TimeProvider
import org.springframework.stereotype.Component

@Component
class DefaultTimeProvider : TimeProvider {
    @Throws(InterruptedException::class)
    override fun sleep(millis: Long) {
        Thread.sleep(millis)
    }
}

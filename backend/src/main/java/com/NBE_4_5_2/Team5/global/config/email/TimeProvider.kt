package com.NBE_4_5_2.Team5.global.config.email

interface TimeProvider {
    @Throws(InterruptedException::class)
     fun sleep(millis: Long)
}
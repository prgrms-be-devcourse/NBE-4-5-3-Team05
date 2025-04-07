package com.NBE_4_5_2.Team5.global.config

import jakarta.transaction.Transactional
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ActiveProfiles("test")
@Import(TestConfig::class)
@Transactional
annotation class BaseTestConfig 


package com.NBE_4_5_2.Team5.global.config;

import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@Testcontainers
@Import(TestConfig.class)
@Transactional
public abstract class BaseTest {}
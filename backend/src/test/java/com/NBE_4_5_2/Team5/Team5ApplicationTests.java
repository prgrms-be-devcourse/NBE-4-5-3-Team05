package com.NBE_4_5_2.Team5;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.NBE_4_5_2.Team5.domain.user.user.service.RedisService;

@SpringBootTest
@ActiveProfiles("test")
class Team5ApplicationTests {

	@MockitoBean
	private RedisService redisService;

	@Test
	void contextLoads() {
	}

}

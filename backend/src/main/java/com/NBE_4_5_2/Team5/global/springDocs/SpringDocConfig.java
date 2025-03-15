package com.NBE_4_5_2.Team5.global.springDocs;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;

@SecurityScheme(
	name = "cookieAuth",
	type = SecuritySchemeType.APIKEY,   // 쿠키 인증
	in = SecuritySchemeIn.COOKIE,      // 쿠키에서 인증 정보 가져오기
	paramName = "Cookie"           // 쿠키의 key 값 (JWT 토큰을 저장한 쿠키 이름)
)

@Configuration
@OpenAPIDefinition(info = @Info(title = "API 서버", version = "v1"))
public class SpringDocConfig {

	@Bean
	public GroupedOpenApi groupApiV1() {
		return GroupedOpenApi.builder()
			.group("api")
			.pathsToMatch("/api/**")
			.addOpenApiCustomizer(setting -> setting
				.addSecurityItem(new SecurityRequirement().addList("cookieAuth")))
			.build();
	}

}
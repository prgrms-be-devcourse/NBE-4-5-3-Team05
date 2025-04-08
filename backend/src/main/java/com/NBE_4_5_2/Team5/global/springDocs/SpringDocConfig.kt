package com.NBE_4_5_2.Team5.global.springDocs

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityRequirement
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SecurityScheme(
    name = "cookieAuth",
    type = SecuritySchemeType.APIKEY,
    `in` = SecuritySchemeIn.COOKIE,
    paramName = "Cookie"
)
@Configuration
@OpenAPIDefinition(info = Info(title = "API 서버", version = "v1"))
class SpringDocConfig {
    @Bean
    fun groupApiV1(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("api")
            .pathsToMatch("/api/**")
            .addOpenApiCustomizer { setting: OpenAPI ->
                setting
                    .addSecurityItem(SecurityRequirement().addList("cookieAuth"))
            }
            .build()
    }
}
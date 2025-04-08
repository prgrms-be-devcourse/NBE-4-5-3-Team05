package com.NBE_4_5_2.Team5.global.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component

@Component
class CustomAuthorizationRequestResolver(
    clientRegistrationRepository: ClientRegistrationRepository
) : OAuth2AuthorizationRequestResolver {

    private val defaultResolver =
        DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository,
            "/oauth2/authorization"
        )

    override fun resolve(request: HttpServletRequest): OAuth2AuthorizationRequest? =
        defaultResolver.resolve(request)
            ?.let { customize(request, it) }

    override fun resolve(
        request: HttpServletRequest,
        clientRegistrationId: String
    ): OAuth2AuthorizationRequest? =
        defaultResolver.resolve(request, clientRegistrationId)
            ?.let { customize(request, it) }

    private fun customize(
        request: HttpServletRequest,
        req: OAuth2AuthorizationRequest
    ): OAuth2AuthorizationRequest {
        request.getParameter("redirectUrl")
            ?.also { request.session.setAttribute("redirectUrl", it) }
        return OAuth2AuthorizationRequest.from(req).build()
    }
}

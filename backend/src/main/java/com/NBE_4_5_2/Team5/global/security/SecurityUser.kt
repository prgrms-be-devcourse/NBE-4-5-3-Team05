package com.NBE_4_5_2.Team5.global.security

import com.NBE_4_5_2.Team5.domain.user.user.entity.Role
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.core.user.OAuth2User

class SecurityUser(
    val id: String,
    username: String?,
    password: String?,
    val nickname: String,
    val role: Role,
    authorities: Collection<GrantedAuthority?>
) :
    User(username, password, authorities), OAuth2User {
    constructor(user: com.NBE_4_5_2.Team5.domain.user.user.entity.User) : this(
        user.id, user.username, user.password,
        user.nickname, user.role, user.authorities
    )

    override fun <A> getAttribute(name: String): A? {
        return super.getAttribute(name)
    }

    override fun getAttributes(): Map<String, Any> {
        return emptyMap()
    }

    override fun getName(): String {
        return this.username
    }
}

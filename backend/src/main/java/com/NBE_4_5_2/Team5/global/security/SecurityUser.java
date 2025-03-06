package com.NBE_4_5_2.Team5.global.security;

import com.NBE_4_5_2.Team5.domain.user.entity.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class SecurityUser extends User implements OAuth2User {

    @Getter
    private String id;

    @Getter
    private Role role;

    @Getter
    private String nickname;

    public SecurityUser(String id, String username, String password, String nickname, Role role, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.role = role;
        this.nickname = nickname;
    }

    public SecurityUser(com.NBE_4_5_2.Team5.domain.user.entity.User user) {
        this(user.getId(), user.getUsername(), user.getPassword(), user.getNickname(), user.getRole(), user.getAuthorities());
    }

    @Override
    public <A> A getAttribute(String name) {
        return OAuth2User.super.getAttribute(name);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public String getName() {
        return this.getUsername();
    }
}

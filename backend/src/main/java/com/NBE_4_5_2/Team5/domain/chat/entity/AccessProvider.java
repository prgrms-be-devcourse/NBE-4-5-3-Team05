package com.NBE_4_5_2.Team5.domain.chat.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AccessProvider {
    private String name;
    private String token;

    @Builder
    public AccessProvider(String name, String token) {
        this.name = name;
        this.token = token;
    }
}
package com.NBE_4_5_2.Team5.domain.user.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Setter
@Getter
public class Users {
    @Id
    private String id;
    private String username;

    public Users() {
        this.id = UUID.randomUUID().toString(); // UUID를 String으로 초기화
    }
}
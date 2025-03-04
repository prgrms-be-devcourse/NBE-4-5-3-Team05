package com.NBE_4_5_2.Team5.domain.user.dto;

import com.NBE_4_5_2.Team5.domain.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserDto {
    private String id;
    private String username;
    private String email;
    private String nickname;
    private String address;
    private String profileUrl;
    private Integer role;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.address = user.getAddress();
        this.profileUrl = user.getProfileUrl();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
        this.modifiedAt = user.getModifiedAt();
    }

    public static UserDto fromEntity(User user) {
        return new UserDto(user);
    }
}

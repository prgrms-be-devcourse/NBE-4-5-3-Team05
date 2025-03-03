package com.NBE_4_5_2.Team5.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    private String nickname;
    private String address;
    private String profileUrl;
    private String email;
}

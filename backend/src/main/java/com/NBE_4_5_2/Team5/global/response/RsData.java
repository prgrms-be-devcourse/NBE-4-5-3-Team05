package com.NBE_4_5_2.Team5.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class RsData<T> {
    private String message;
    private String code;
    private T data;
}

package com.NBE_4_5_2.Team5.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class RsData<T> {
    private String code;
    private String message;
    private T data;
}

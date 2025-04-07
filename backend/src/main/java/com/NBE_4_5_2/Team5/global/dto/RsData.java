package com.NBE_4_5_2.Team5.global.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@NoArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RsData<T> {
    @NonNull
    private String code;
    @NonNull
    private String message;
    @NonNull
    private T data;

    public RsData(String code, String message) {
        this(code, message, (T) new Empty());
    }

    public RsData(@NonNull String code, @NonNull String message, @NonNull T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    @NonNull
    public String getCode() {
        return code;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    @NonNull
    public T getData() {
        return data;
    }

    @JsonIgnore
    public int getStatusCode() {
        String statusCodeStr = code.split("-")[0];
        return Integer.parseInt(statusCodeStr);
    }
}

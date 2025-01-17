package com.elice.aurasphere.global.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Builder
@Getter
public class ApiRes<T> {

    private final HttpStatus status;
    private final String message;
    private final T data;

    public static <T> ApiRes<T> successRes(HttpStatus status, T data) {
        return new ApiRes<>(status, "Success", data); // 기본 메시지 사용
    }

    public static <T> ApiRes<T> failureRes(HttpStatus status, String message, T data) {
        return new ApiRes<>(status, message, data);
    }

    public static <T> ApiRes<T> errorRes(HttpStatus status, String message, T data) {
        return new ApiRes<>(status, message, data);
    }
}

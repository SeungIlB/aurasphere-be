package com.elice.aurasphere.global.common;

import com.elice.aurasphere.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ApiResponseDto<T> extends ResponseDto {

    private final T data;

    private ApiResponseDto(T data) {
        super(ErrorCode.OK.getCode(), ErrorCode.OK.getMessage());
        this.data = data;
    }

    public static <T> ApiResponseDto<T> from(T data) {
        return new ApiResponseDto<>(data);
    }
}
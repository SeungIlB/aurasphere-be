package com.elice.aurasphere.global.common;

import com.elice.aurasphere.global.exception.ValidExceptionResponse;
import lombok.Getter;

@Getter
public class ValidResponseDTO {

    private final String code; // 응답 코드
    private final ValidExceptionResponse data; // 응답 코드

    // 생성자
    protected ValidResponseDTO(String code, ValidExceptionResponse data) {
        this.code = code;
        this.data = data;
    }
}

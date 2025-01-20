package com.elice.aurasphere.global.common;


import lombok.Getter;

@Getter
public class ResponseDto {

    private final String code; // 응답 코드
    private final String message; // 응답 메시지

    // 생성자
    protected ResponseDto(String code, String message) {
        this.code = code;
        this.message = message;
    }
}

package com.elice.aurasphere.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private String code;

    public ErrorResponse(String message) {
        this.message = message;
        this.code = "ERROR";
    }
}
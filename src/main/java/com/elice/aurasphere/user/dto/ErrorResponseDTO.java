package com.elice.aurasphere.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponseDTO {
    private String message;
    private String code;

    public ErrorResponseDTO(String message) {
        this.message = message;
        this.code = "ERROR";
    }
}
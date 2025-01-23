package com.elice.aurasphere.user.dto;

import lombok.Getter;

@Getter
public class PasswordResetRequestDTO {
    private String email;
    private String newPassword;
}

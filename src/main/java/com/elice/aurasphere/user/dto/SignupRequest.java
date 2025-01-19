package com.elice.aurasphere.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {
    @NotBlank(message = "이름은 필수 입력사항입니다")
    @Size(max = 10, message = "이름은 10자 이하여야 합니다")
    private String name;

    @NotBlank(message = "닉네임은 필수 입력사항입니다")
    @Size(max = 20, message = "닉네임은 20자 이하여야 합니다")
    private String nickname;

    @NotBlank(message = "이메일은 필수 입력사항입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력사항입니다")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    private String password;
}
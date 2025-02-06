package com.elice.aurasphere.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequestDTO {
    @Schema(description = "사용자 닉네임", example = "길동이", minLength = 1, maxLength = 20)
    @NotBlank(message = "닉네임은 필수 입력사항입니다")
    @Size(max = 20, message = "닉네임은 20자 이하여야 합니다")
    private String nickname;

    @Schema(description = "사용자 이메일", example = "user@example.com")
    @NotBlank(message = "이메일은 필수 입력사항입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @Schema(description = "비밀번호는 최소 8자 이상이며, 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다", example = "Password123!", minLength = 8)
    @NotBlank(message = "비밀번호는 필수 입력사항입니다")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "비밀번호는 최소 8자 이상이며, 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다")
    private String password;
}
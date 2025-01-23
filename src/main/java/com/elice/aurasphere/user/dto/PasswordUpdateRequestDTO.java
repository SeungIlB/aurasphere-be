package com.elice.aurasphere.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "비밀번호 수정 요청 DTO")
@Getter
@NoArgsConstructor
public class PasswordUpdateRequestDTO {
    @Schema(description = "현재 비밀번호")
    @NotBlank(message = "현재 비밀번호는 필수 입력사항입니다")
    private String currentPassword;

    @Schema(description = "현재 비밀번호 확인")
    @NotBlank(message = "현재 비밀번호 확인은 필수 입력사항입니다")
    private String currentPasswordConfirm;

    @Schema(description = "비밀번호는 최소 8자 이상이며, 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다", example = "Password123!", minLength = 8)
    @NotBlank(message = "비밀번호는 필수 입력사항입니다")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "비밀번호는 최소 8자 이상이며, 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다")
    private String newPassword;
}
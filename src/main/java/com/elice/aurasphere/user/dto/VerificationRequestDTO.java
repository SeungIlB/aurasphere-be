package com.elice.aurasphere.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@NoArgsConstructor
public class VerificationRequestDTO {
    @NotBlank(message = "인증 코드는 필수 입력사항입니다")
    @Size(min = 6, max = 6, message = "인증 코드는 6자리여야 합니다")
    private String code;

    // 세션이나 토큰에서 이메일을 가져올 수 없는 경우를 대비해 이메일도 유지
    @NotBlank(message = "이메일은 필수 입력사항입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
}
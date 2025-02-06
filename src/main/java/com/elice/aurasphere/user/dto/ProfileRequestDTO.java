package com.elice.aurasphere.user.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "프로필 수정 요청 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileRequestDTO {
    @Schema(description = "변경할 닉네임 (선택)")
    @Size(max = 20, message = "닉네임은 20자 이하여야 합니다")
    private String nickname;

    @Schema(description = "S3에 업로드된 이미지의 key (선택)")
    private String imageKey;
}

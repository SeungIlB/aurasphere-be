package com.elice.aurasphere.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "프로필 수정 응답 DTO")
@Getter
@Builder
public class ProfileResponse {
    @Schema(description = "사용자 닉네임")
    private String nickname;

    @Schema(description = "프로필 이미지 URL ('DEFAULT' 또는 S3 URL)")
    private String profileUrl;
}
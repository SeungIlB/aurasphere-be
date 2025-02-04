package com.elice.aurasphere.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "프로필 수정 응답 DTO")
@Getter
@Builder
public class ProfileResponseDTO {
    @Schema(description = "사용자 닉네임")
    private String nickname;

    @Schema(description = "프로필 이미지 URL")
    private String profileUrl;

    @Schema(description = "작성한 게시글 수")
    private Long postsCount;

    @Schema(description = "팔로워 수")
    private Long followerCount;

    @Schema(description = "팔로잉 수")
    private Long followingCount;
}
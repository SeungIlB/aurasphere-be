package com.elice.aurasphere.user.dto;

import com.elice.aurasphere.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "팔로워/팔로잉 정보 응답 DTO")
@Getter
@Builder
public class FollowUserResponse {
    @Schema(description = "사용자 ID")
    private Long userId;

    @Schema(description = "사용자 이메일")
    private String email;

    @Schema(description = "프로필 닉네임")
    private String nickname;

    @Schema(description = "프로필 이미지 URL")
    private String profileUrl;

    public static FollowUserResponse from(User user) {
        return FollowUserResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .nickname(user.getProfile() != null ? user.getProfile().getNickname() : null)
            .profileUrl(user.getProfile() != null ? user.getProfile().getProfileUrl() : null)
            .build();
    }
}
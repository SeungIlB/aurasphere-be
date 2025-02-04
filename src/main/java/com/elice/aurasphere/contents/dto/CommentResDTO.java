package com.elice.aurasphere.contents.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "댓글 응답 DTO")
@Builder
@Getter
public class CommentResDTO {

    @Schema(description = "유저 id")
    private Long id;

    @Schema(description = "유저 닉네임")
    private String nickname;

    @Schema(description = "댓글 내용")
    private String content;

    @Schema(description = "생성시간")
    private LocalDateTime createdAt;

    @Schema(description = "수정시간")
    private LocalDateTime updatedAt;


    @Builder
    public CommentResDTO(
            Long id,
            String nickname,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.nickname = nickname;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
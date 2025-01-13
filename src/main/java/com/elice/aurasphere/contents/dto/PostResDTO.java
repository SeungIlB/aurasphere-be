package com.elice.aurasphere.contents.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;


@Schema(description = "게시글 응답 DTO")
@Getter
public class PostResDTO {

    @Schema(description = "게시글 id")
    private Long id;

    @Schema(description = "게시글 내용")
    private String content;

    @Schema(description = "게시글 좋아요 수")
    private Long likes;

    @Schema(description = "게시글의 총 댓글 수")
    private Long commentCnt;

    @Schema(description = "게시글 최초 작성 시간")
    private LocalDateTime createAt;

    @Schema(description = "게시글 최종 수정 시간")
    private LocalDateTime updateAt;


    @Builder
    public PostResDTO(
            Long id,
            String content,
            Long likes,
            Long commentCnt,
            LocalDateTime createAt,
            LocalDateTime updateAt) {

        this.id = id;
        this.content = content;
        this.likes = likes;
        this.commentCnt = commentCnt;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

}

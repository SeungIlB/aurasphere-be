package com.elice.aurasphere.contents.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;


@Schema(description = "게시글 응답 DTO")
@Getter
public class PostResDTO {

    @Schema(description = "게시글 id")
    private Long id;

    @Schema(description = "게시글 내용")
    private String content;

    @Schema(description = "게시글 좋아요 수")
    private Long likeCnt;

    @Schema(description = "게시글의 총 댓글 수")
    private Long commentCnt;

    @Schema(description = "컨텐츠의 url")
    private List<String> imgUrls;

    @Schema(description = "게시글 최초 작성 시간")
    private LocalDateTime createdAt;

    @Schema(description = "게시글 최종 수정 시간")
    private LocalDateTime updatedAt;


    @Builder
    public PostResDTO(
            Long id,
            String content,
            Long likeCnt,
            Long commentCnt,
            List<String> imgUrls,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.content = content;
        this.likeCnt = likeCnt;
        this.commentCnt = commentCnt;
        this.imgUrls = imgUrls;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}

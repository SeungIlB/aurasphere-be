package com.elice.aurasphere.contents.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;


@Schema(description = "게시글 응답 DTO")
@Getter
public class PostResponseDTO {

    @Schema(description = "게시글 id")
    private Long id;

    @Schema(description = "게시글 작성자 닉네임")
    private String nickname;

    @Schema(description = "게시글 작성자 프로필")
    private String profileUrl;

    @Schema(description = "게시글 내용")
    private String content;

    @Schema(description = "게시글 좋아요 수")
    private Long likeCnt;

    @Schema(description = "게시글의 조회수")
    private Long viewCnt;

    @Schema(description = "현재 로그인된 사용자가 좋아요를 눌렀는 지 여부")
    private boolean isLiked;

    @Schema(description = "게시글의 총 댓글 수")
    private Long commentCnt;

    @Schema(description = "컨텐츠의 url")
    private List<FileDTO> urls;

    @Schema(description = "게시글 최초 작성 시간")
    private LocalDateTime createdAt;

    @Schema(description = "게시글 최종 수정 시간")
    private LocalDateTime updatedAt;


    @Builder
    public PostResponseDTO(
            Long id,
            String nickname,
            String profileUrl,
            String content,
            Long likeCnt,
            Long viewCnt,
            boolean isLiked,
            Long commentCnt,
            List<FileDTO> urls,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.nickname = nickname;
        this.profileUrl = profileUrl;
        this.content = content;
        this.likeCnt = likeCnt;
        this.viewCnt = viewCnt;
        this.isLiked = isLiked;
        this.commentCnt = commentCnt;
        this.urls = urls;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void setLiked(boolean isLiked){
        this.isLiked = isLiked;
    }
}

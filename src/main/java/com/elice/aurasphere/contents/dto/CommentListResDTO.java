package com.elice.aurasphere.contents.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Schema(description = "댓글 리스트 응답 DTO")
@Getter
public class CommentListResDTO {

    @Schema(description = "댓글 리스트")
    private List<CommentResDTO> commentList;

    @Schema(description = "마지막 댓글 번호")
    private Long comment_cursor;

    @Schema(description = "다음 데이터가 있는 지의 여부\n다음 데이터가 있는 경우 : true\n마지막 데이터까지 받은 경우 : false")
    private Boolean hasNext;

    @Builder
    public CommentListResDTO(
            List<CommentResDTO> commentList,
            Long comment_cursor,
            Boolean hasNext
    ){
        this.commentList = commentList;
        this.comment_cursor = comment_cursor;
        this.hasNext = hasNext;
    }
}

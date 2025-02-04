package com.elice.aurasphere.contents.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Schema(description = "게시글 리스트 응답 DTO")
@Getter
public class PostListResDTO {

    @Schema(description = "게시글 정보")
    private List<PostResDTO> postList;

    @Schema(description = "마지막 게시글 번호")
    private Long post_cursor;

    @Schema(description = "마지막 게시글의 필터링 변수를 기준으로 하는 커서 값")
    private Long filter_cursor;

    @Schema(description = "다음 데이터가 있는 지의 여부\n다음 데이터가 있는 경우 : true\n마지막 데이터까지 받은 경우 : false")
    private Boolean hasNext;

    @Builder
    public PostListResDTO(
            List<PostResDTO> postList,
            Long post_cursor,
            Long filter_cursor,
            Boolean hasNext
    ){
        this.postList = postList;
        this.post_cursor = post_cursor;
        this.filter_cursor = filter_cursor;
        this.hasNext = hasNext;
    }
}
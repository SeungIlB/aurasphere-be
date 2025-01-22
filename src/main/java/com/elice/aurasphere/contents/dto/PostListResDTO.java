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
    private Long cursor;

    @Schema(description = "다음 데이터가 있는 지의 여부\n다음 데이터가 있는 경우 : true\n마지막 데이터까지 받은 경우 : false")
    private Boolean hasNext;


    @Builder
    public PostListResDTO(
            List<PostResDTO> postList,
            Long cursor,
            Boolean hasNext
    ){
        this.postList = postList;
        this.cursor = cursor;
        this.hasNext = hasNext;
    }

}
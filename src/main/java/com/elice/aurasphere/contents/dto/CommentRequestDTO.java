package com.elice.aurasphere.contents.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "댓글 요청 DTO")
@NoArgsConstructor
@Getter
public class CommentRequestDTO {

    @Schema(description = "댓글 내용")
    private String content;
}

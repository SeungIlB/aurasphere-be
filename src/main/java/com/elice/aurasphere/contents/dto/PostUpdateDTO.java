package com.elice.aurasphere.contents.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Schema(description = "게시글 PATCH 요청 DTO")
@Getter
public class PostUpdateDTO {

    @Schema(description = "게시글 내용")
    @Size(max = 150, message = "내용은 150자 이하여야 합니다.")
    private String content;

}

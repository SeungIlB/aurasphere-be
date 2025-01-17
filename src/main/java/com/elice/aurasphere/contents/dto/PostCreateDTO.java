package com.elice.aurasphere.contents.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;


@Schema(description = "게시글 POST 요청 DTO")
@Getter
public class PostCreateDTO {

    @Schema(description = "게시글 내용")
    @NotNull
    @NotEmpty(message = "내용은 필수 입력 항목입니다.")
    @Size(max = 150, message = "내용은 150자 이하여야 합니다.")
    private String content;

}
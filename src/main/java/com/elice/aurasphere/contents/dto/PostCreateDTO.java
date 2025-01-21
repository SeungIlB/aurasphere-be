package com.elice.aurasphere.contents.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;


@Schema(description = "게시글 등록 요청 DTO")
@Getter
public class PostCreateDTO {

    @Schema(description = "게시글 내용")
    @Size(max = 150, message = "내용은 150자 이하여야 합니다.")
    private String content;

    @Schema(description = "이미지 key 리스트")
    @NotNull
    @NotEmpty(message = "이미지는 한 개 이상이 포함되어야 합니다.")
    @Size(min = 1, max = 5, message = "이미지는 최소 1개에서 최대 5개까지 포함되어야 합니다.")
    private List<String> imgKeys;

}
package com.elice.aurasphere.contents.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "게시글 요청 DTO")
@Getter
public class PostReqDTO {

    @Size(max = 150, message = "게시글 내용은 150자 이하여야 합니다.")
    @Schema(description = "게시글 내용")
    private String content;

    @Schema(description = "게시글 사진 또는 동영상")
    private List<MultipartFile> files = new ArrayList<>();

}

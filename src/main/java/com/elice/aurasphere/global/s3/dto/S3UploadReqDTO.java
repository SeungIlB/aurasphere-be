package com.elice.aurasphere.global.s3.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Schema(description = "파일 업로드를 위한 presigned-url 요청 DTO입니다. \n 파일 이름이랑 파일형식을 맞게 넣어주세요.")
@Getter
public class S3UploadReqDTO {


    @Schema(description = "파일명")
    @NotEmpty(message = "파일명은 필수 입력 항목입니다.")
    private String fileName;

    @Schema(description = "파일 형식")
    @NotEmpty(message = "파일 형식은 필수 입력 항목입니다.")
    private String contentType;

}

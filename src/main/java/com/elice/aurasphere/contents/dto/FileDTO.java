package com.elice.aurasphere.contents.dto;


import com.elice.aurasphere.contents.entity.File;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "게시글 응답에 들어가는 FileDTO")
@Getter
@Setter
public class FileDTO {

    @Schema(description = "파일 타입")
    private File.FileType fileType;

    @Schema(description = "파일의 url")
    private String url;


    @Builder
    public FileDTO(File.FileType fileType, String url){
        this.fileType = fileType;
        this.url = url;
    }

}

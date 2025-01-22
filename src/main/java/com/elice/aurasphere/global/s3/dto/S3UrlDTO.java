package com.elice.aurasphere.global.s3.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class S3UrlDTO {

    private String presignedUrl;

    private String key;


    @Builder
    public S3UrlDTO(String presignedUrl, String key){
        this.presignedUrl = presignedUrl;
        this.key = key;
    }

}

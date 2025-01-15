package com.elice.aurasphere.contents.controller;


import com.elice.aurasphere.contents.dto.S3UrlDTO;
import com.elice.aurasphere.contents.service.S3Service;
import com.elice.aurasphere.global.common.ApiRes;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/s3")
@RestController
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    //이미지 업로드용 presigned-url 요청 api
    @GetMapping(value = "/puturl")
    public ApiRes<S3UrlDTO> getPostS3Url(@RequestParam("file-name") String fileName,@RequestParam("content-type") String contentType) {
        S3UrlDTO getS3UrlDto = s3Service.getPutS3Url(fileName, contentType);
        return new ApiRes<>(HttpStatus.OK, "success", getS3UrlDto);
    }


    /*
    이미지 조회 시 보내줄 presigned-url
    근데 클라이언트에서 직접적으로 요청하는 경우는 없음. 임시 테스트용
    클라이언트는 게시글 조회 혹은 프로필 이미지 조회 api로 presigned-url 획득
    이후에는 db에서 key값을 가져와서 보여줄 예정.
     */
    @GetMapping(value = "/geturl")
    public ApiRes<S3UrlDTO> getGetS3Url(@RequestParam("key") String key) {

        S3UrlDTO getS3UrlDto = s3Service.getGetS3Url(key);
        return new ApiRes<>(HttpStatus.OK, "success", getS3UrlDto);
    }
}

package com.elice.aurasphere.global.s3;


import com.elice.aurasphere.global.s3.service.S3Service;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/s3")
@RestController
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    //게시판 이미지 업로드용 presigned-url 요청 api
//    @Operation(summary = "S3 업로드 url 요청 API", description = "S3 이미지 업로드 권한 요청하는 API입니다.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "201", description = "S3 업로드 url 요청 성공"),
//            @ApiResponse(responseCode = "400", description = "S3 업로드 url 요청 실패"),
//    })
//    @PostMapping(value = "/url")
//    public ApiRes<List<S3UrlDTO>> getPostS3Url(
//            @Valid @RequestBody ArrayList<S3UploadReqDTO> s3UploadReqDTOList
//    ) {
//
//        List<S3UrlDTO> getS3UrlDto = s3Service.getPutS3Url(s3UploadReqDTOList);
//
//        return new ApiRes<>(HttpStatus.CREATED, "success", getS3UrlDto);
//    }

}

package com.elice.aurasphere.contents.controller;


import com.elice.aurasphere.contents.dto.PostResDTO;
import com.elice.aurasphere.contents.dto.S3UploadReqDTO;
import com.elice.aurasphere.contents.dto.S3UrlDTO;
import com.elice.aurasphere.contents.service.S3Service;
import com.elice.aurasphere.global.common.ApiRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RequestMapping("/s3")
@RestController
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    //이미지 업로드용 presigned-url 요청 api
    @Operation(summary = "S3 업로드 url 요청 API", description = "S3 이미지 업로드 권한 요청하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "S3 업로드 url 요청 성공",
                    content = {@Content(schema = @Schema(implementation = PostResDTO.class))}),
            @ApiResponse(responseCode = "400", description = "S3 업로드 url 요청 실패"),
    })
    @PostMapping(value = "/url")
    public ApiRes<List<S3UrlDTO>> getPostS3Url(
            @Valid @RequestBody ArrayList<S3UploadReqDTO> s3UploadReqDTOList
    ) {

//        List<S3UploadReqDTO> s3UploadReqDTOs = new ArrayList<>();

        List<S3UrlDTO> getS3UrlDto = s3Service.getPutS3Url(s3UploadReqDTOList);

        return new ApiRes<>(HttpStatus.CREATED, "success", getS3UrlDto);
    }

//    /*
//    이미지 조회 시 보내줄 presigned-url
//    근데 클라이언트에서 직접적으로 요청하는 경우는 없음. 임시 테스트용
//    클라이언트는 게시글 조회 혹은 프로필 이미지 조회 api로 presigned-url 획득
//    이후에는 db에서 key값을 가져와서 보여줄 예정.
//     */
//    @Operation(summary = "S3 읽어오기 url 요청 API", description = "S3 이미지 불러오는 API입니다.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "201", description = "S3 읽어오기 url 요청 성공",
//                    content = {@Content(schema = @Schema(implementation = PostResDTO.class))}),
//            @ApiResponse(responseCode = "400", description = "S3 읽어오기 url 요청 실패"),
//    })
//    @GetMapping(value = "/geturl")
//    public ApiRes<S3UrlDTO> getGetS3Url(@RequestParam("key") String key) {
//
//        S3UrlDTO getS3UrlDto = s3Service.getGetS3Url(key);
//        return new ApiRes<>(HttpStatus.OK, "success", getS3UrlDto);
//    }
}

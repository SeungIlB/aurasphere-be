package com.elice.aurasphere.contents.controller;


import com.elice.aurasphere.contents.dto.PostCreateDTO;
import com.elice.aurasphere.contents.dto.PostResDTO;
import com.elice.aurasphere.contents.dto.PostUpdateDTO;
import com.elice.aurasphere.contents.service.PostService;
import com.elice.aurasphere.contents.service.S3Service;
import com.elice.aurasphere.global.common.ApiRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Post", description = "게시글 API")
@Slf4j
@RequestMapping("/post")
@RestController
public class PostController {

    private final PostService postService;
    private final S3Service s3Service;

    public PostController(PostService postService, S3Service s3Service) {
        this.postService = postService;
        this.s3Service = s3Service;
    }


    //게시글 조회 api
    @Operation(summary = "게시글 조회 API", description = "상세 게시글(1개)을 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
            @ApiResponse(responseCode = "400", description = "게시글 조회 실패"),
    })
    @GetMapping("/{postId}")
    public ApiRes<PostResDTO> readPostByPostId(
            @PathVariable("postId") Long postId
    ){

//        PostResDTO post = postService.getPost(userDetails.getUsername(), postCreateDTO);

        PostResDTO postResDTO = postService.getPost(postId);

        return ApiRes.successRes(HttpStatus.OK, postResDTO);
    }

    //글 작성 api
    @Operation(summary = "게시글 작성 API", description = "게시글을 작성하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "게시글 등록 성공"),
            @ApiResponse(responseCode = "400", description = "게시글 등록 실패"),
    })
    @PostMapping
    public ApiRes<PostResDTO> createPost(
            @Valid @RequestBody PostCreateDTO postCreateDTO
    ){

//        PostResDTO post = postService.processPost(userDetails.getUsername(), postCreateDTO);

        PostResDTO postResDTO = postService.registerPost(postCreateDTO);

        return ApiRes.successRes(HttpStatus.CREATED, postResDTO);
    }


    //글 수정 api
    @Operation(summary = "게시글 수정 API", description = "게시글을 수정하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "400", description = "게시글 등록 실패"),
    })
    @PatchMapping("/{postId}")
    public ApiRes<PostResDTO> updatePost(
            @PathVariable("postId") Long postId,
            @Valid @RequestBody PostUpdateDTO postUpdateDTO
    ){

        PostResDTO postResDTO = postService.editPost(postId, postUpdateDTO);

        return ApiRes.successRes(HttpStatus.OK, postResDTO);
    }

}
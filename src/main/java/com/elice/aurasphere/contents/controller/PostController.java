package com.elice.aurasphere.contents.controller;


import com.elice.aurasphere.contents.dto.*;
import com.elice.aurasphere.user.entity.CustomUserDetails;
import com.elice.aurasphere.contents.service.LikeService;
import com.elice.aurasphere.contents.service.PostService;
import com.elice.aurasphere.global.common.ApiResponseDto;
import com.elice.aurasphere.global.common.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Tag(name = "Post", description = "게시글 API \n 모든 api Access Token 필요")
@Slf4j
@RestController
public class PostController {

    private final PostService postService;
    private final LikeService likeService;

    public PostController(PostService postService, LikeService likeService) {
        this.postService = postService;
        this.likeService = likeService;
    }

    /*
    게시글 필터별로 조회하는 api
    필터링 없이 그냥 조회했을 경우(모든 글 최신순), 좋아요 수 기준, 조회수 기준, 내가 팔로우한 사람만
    */
    @GetMapping("/posts")
    public ApiResponseDto<PostListResDTO> readPostsByFilter(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "cursor", defaultValue = "0") Long cursor,
            @RequestParam(value = "filter", required = false) String filter
    ){

        PostListResDTO postListResDTO = postService.getFilteredPosts(
                userDetails.getUsername(), size, cursor, filter
        );

        return ApiResponseDto.from(null);
    }


    /*
    내가 작성한 게시글 List 조회하는 api
     */
    @Operation(
            summary = "내가 작성한 게시글 조회 API",
            description = "현재 로그인되어 있는 유저가 작성한 게시글(내 게시글)들을 조회하는 API"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "S000",
                    description = "게시글 조회 성공"),
            @ApiResponse(
                    responseCode = "P001",
                    description = "게시글을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ResponseDto.class)))
    })
    @GetMapping("/posts/me")
    public ApiResponseDto<PostListResDTO> readPostsByUserId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "cursor", defaultValue = "0") Long cursor
            ){

        PostListResDTO postListResDTO = postService.getMyPosts(userDetails.getUsername(), size, cursor);

        return ApiResponseDto.from(postListResDTO);
    }



    /*
    특정 게시글 1개만 조회하는 api
    */
    @Operation(summary = "게시글 조회 API", description = "상세 게시글(1개)을 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "S000",
                    description = "게시글 조회 성공"),
            @ApiResponse(responseCode = "P001",
                    description = "게시글을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ResponseDto.class)))
    })
    @GetMapping("/posts/{postId}")
    public ApiResponseDto<PostResDTO> readPostByPostId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("postId") Long postId,
            HttpServletRequest request,
            HttpServletResponse response
    ){

        PostResDTO postResDTO = postService.getPost(userDetails.getUsername(), postId);
        postService.incrementViewCnt(userDetails.getUsername(), postId, request, response);

        return ApiResponseDto.from(postResDTO);
    }

    /*
    게시글 작성하는 api
    */
    @Operation(summary = "게시글 작성 API", description = "게시글을 작성하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "S000",
                    description = "게시글 등록 성공"),
            @ApiResponse(responseCode = "U001",
                    description = "유저를 찾을 수 없는 경우")
    })
    @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDto<PostResDTO> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "content") String content,
            @RequestPart(value = "files") List<MultipartFile> files
            ) throws IOException {

        PostResDTO postResDTO = postService.registerPost(
                userDetails.getUsername(),
                content,
                files);

        return ApiResponseDto.from(postResDTO);
    }

    /*
    좋아요 누르기 / 취소 api
    */
    @PostMapping("/posts/{postId}/like")
    public ApiResponseDto<Boolean> likePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("postId") Long postId) {


        boolean result = likeService.toggleLike(userDetails.getUsername(), postId);

        return ApiResponseDto.from(result);
    }


    /*
    게시글 수정하는 api
    글 내용만 수정 가능
    */
    @Operation(summary = "게시글 수정 API", description = "게시글을 수정하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "S000",
                    description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "P001",
                    description = "게시글을 찾을 수 없는 경우"),
            @ApiResponse(responseCode = "U001",
                    description = "유저를 찾을 수 없는 경우"),
            @ApiResponse(responseCode = "U002",
                    description = "로그인된 유저가 게시글 작성자가 아닌 경우")
    })
    @PatchMapping("/posts/{postId}")
    public ApiResponseDto<PostResDTO> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("postId") Long postId,
            @Valid @RequestBody PostUpdateDTO postUpdateDTO
    ){

        PostResDTO postResDTO = postService.editPost(userDetails.getUsername(), postId, postUpdateDTO);

        return ApiResponseDto.from(postResDTO);
    }


    /*
    게시글 삭제하는 api (soft delete)
    */



}
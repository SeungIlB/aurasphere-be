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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Tag(name = "Post", description = "게시글 API \n 모든 api Access Token 필요")
@Slf4j
@RestController
@RequestMapping("/api")
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
    @Operation(summary = "게시글 리스트 필터링 조회 API", description = "필터 별로 게시글을 조회하는 API입니다. <br><br>" +
            "post_cursor는 마지막 게시글의 id를 담은 커서이고, filter_cursor는 마지막 게시글의 필터값의 숫자를 담은 커서입니다. <br>" +
            "이 값을 사용하여 다음 페이지의 게시글을 가져오는 데 활용됩니다. <br>" +
            "예를 들어, 이전 요청에서 반환된 게시글 리스트의 마지막 게시글이 5개의 좋아요를 가지고 있다면, <br>" +
            "filter_cursor는 5가 반환됩니다. <br>" +
            "<br>첫 페이지를 요청할 시에는 size만 요청하거나(기본 최신순), size, filter 만 요청해주세요.(좋아요, 조회수, 팔로우) <br>" +
            "<br>filter_cursor, filter 에 아무것도 넣지 않는 경우 : 모든 글 최신순(post_cursor 필요)<br>" +
            "<br> filter 에 들어갈 변수 목록" +
            "<br>likes : 좋아요 순(post_cursor, filter_cursor 필요)" +
            "<br>views : 조회수 순(post_cursor, filter_cursor 필요)" +
            "<br>following : 내가 팔로우 한 사람들(post_cursor 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "S000",
                    description = "게시글 조회 성공"),
            @ApiResponse(responseCode = "P001",
                    description = "게시글을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ResponseDto.class)))
    })
    @GetMapping("/posts")
    public ApiResponseDto<PostListResDTO> readPostsByFilter(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "post_cursor", defaultValue = "0") Long post_cursor,
            @RequestParam(value = "filter_cursor") Optional<Long> filter_cursor,
            @RequestParam(value = "filter") Optional<String> filter
    ){

        PostListResDTO postListResDTO = postService.getFilteredPosts(
                userDetails.getUsername(), size, post_cursor, filter_cursor, filter
        );

        return ApiResponseDto.from(postListResDTO);
    }


    /*
    내가 작성한 게시글 List 조회하는 api
     */
    @Operation(
            summary = "내가 작성한 게시글 조회 API",
            description = "현재 로그인되어 있는 유저가 작성한 게시글(내 게시글)들을 조회하는 API" +
                    "<br>첫 페이지 요청 시에는 cursor값을 0이나 null로 요청, 이후 페이지는 반환되는 cursor를 담아서 요청"
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
                    description = "유저를 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ResponseDto.class)))
    })
    @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDto<PostResDTO> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "content", required = false) Optional<String> content,
            @RequestPart(value = "files") List<MultipartFile> files
//            @ModelAttribute @Valid PostReqDTO postReqDTO
    ) throws IOException {

        String postContent = content.orElse("");

        PostResDTO postResDTO = postService.registerPost(
                userDetails.getUsername(),
                postContent,
                files);

        log.info("postResDTO : {}", postResDTO);

        return ApiResponseDto.from(postResDTO);
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
                    description = "게시글을 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "U001",
                    description = "유저를 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "U002",
                    description = "로그인된 유저가 게시글 작성자가 아닌 경우",
                    content = @Content(schema = @Schema(implementation = ResponseDto.class)))
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
    좋아요 누르기 / 취소 api
    */
    @Operation(summary = "좋아요 누르기 API", description = "좋아요 API입니다. " +
            "<br>반환값 <br>true : 좋아요를 누름 <br>false : 좋아요 취소")
    @PostMapping("/posts/{postId}/like")
    public ApiResponseDto<Boolean> likePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("postId") Long postId) {


        boolean result = likeService.toggleLike(userDetails.getUsername(), postId);

        return ApiResponseDto.from(result);
    }


    /*
    게시글 삭제하는 api (soft delete)
    */
    @Operation(summary = "게시글 삭제 API", description = "게시글을 삭제하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "S000",
                    description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = "P001",
                    description = "게시글을 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "U001",
                    description = "유저를 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "U002",
                    description = "로그인된 유저가 게시글 작성자가 아닌 경우",
                    content = @Content(schema = @Schema(implementation = ResponseDto.class)))
    })
    @PatchMapping("/posts/{postId}/delete")
    public ApiResponseDto<Long> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("postId") Long postId
    ){

        Long deletedPostId = postService.removePost(userDetails.getUsername(), postId);

        return ApiResponseDto.from(deletedPostId);
    }

}
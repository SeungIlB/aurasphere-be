package com.elice.aurasphere.contents.controller;


import com.elice.aurasphere.contents.dto.CommentListResDTO;
import com.elice.aurasphere.contents.dto.CommentReqDTO;
import com.elice.aurasphere.contents.dto.CommentResDTO;
import com.elice.aurasphere.contents.dto.PostReqDTO;
import com.elice.aurasphere.contents.service.CommentService;
import com.elice.aurasphere.global.common.ApiResponseDto;
import com.elice.aurasphere.global.common.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comment", description = "댓글 API")
@Slf4j
@RequestMapping("/api")
@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(
            summary = "댓글 리스트 조회 API",
            description = "특정 포스트에 달린 댓글을 조회하는 API" +
                    "<br>첫 페이지 요청 시에는 cursor값을 0이나 null로 요청, 이후 페이지는 반환되는 cursor를 담아서 요청"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "S000",
                    description = "댓글 리스트 조회 성공"),
    })
    @GetMapping("/comments/{postId}")
    public ApiResponseDto<CommentListResDTO> readPostComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable(name = "postId") Long postId,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "cursor", defaultValue = "0") Long cursor

    ){
        CommentListResDTO commentListResDTO = commentService.getCommentList(
                userDetails.getUsername(), postId, size, cursor);

        return ApiResponseDto.from(commentListResDTO);
    }


    @Operation(
            summary = "댓글 작성 API",
            description = "댓글을 작성하는 API"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "S000",
                    description = "댓글 작성 성공"),
    })
    @PostMapping("/comment/{postId}")
    public ApiResponseDto<CommentResDTO> registerPostComment(
            @PathVariable(name = "postId") Long postId,
            @Valid @RequestBody CommentReqDTO commentReqDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        CommentResDTO postCommentResponseDTO =
                commentService.createPostComment(userDetails.getUsername(), postId, commentReqDTO);

        return ApiResponseDto.from(postCommentResponseDTO);
    }



}
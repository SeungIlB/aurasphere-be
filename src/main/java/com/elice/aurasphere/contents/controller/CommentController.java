package com.elice.aurasphere.contents.controller;


import com.elice.aurasphere.contents.dto.CommentListResponseDTO;
import com.elice.aurasphere.contents.dto.CommentRequestDTO;
import com.elice.aurasphere.contents.dto.CommentResponseDTO;
import com.elice.aurasphere.contents.service.CommentService;
import com.elice.aurasphere.global.common.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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
    public ApiResponseDto<CommentListResponseDTO> readPostComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable(name = "postId") Long postId,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "cursor", defaultValue = "0") Long cursor

    ){
        CommentListResponseDTO commentListResponseDTO = commentService.getCommentList(
                userDetails.getUsername(), postId, size, cursor);

        return ApiResponseDto.from(commentListResponseDTO);
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
    public ApiResponseDto<CommentResponseDTO> registerPostComment(
            @PathVariable(name = "postId") Long postId,
            @Valid @RequestBody CommentRequestDTO commentRequestDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        CommentResponseDTO postCommentResponseDTO =
                commentService.createPostComment(userDetails.getUsername(), postId, commentRequestDTO);

        return ApiResponseDto.from(postCommentResponseDTO);
    }

    @Operation(
            summary = "댓글 수정 API",
            description = "댓글을 수정하는 API"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "S000",
                    description = "댓글 수정 성공"),
    })
    @PatchMapping("/comment/{postId}/{commentId}")
    public ApiResponseDto<CommentResponseDTO> editPostComment(
            @PathVariable(name = "postId") Long postId,
            @PathVariable(name = "commentId") Long commentId,
            @Valid @RequestBody CommentRequestDTO commentRequestDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        CommentResponseDTO postCommentResponseDTO =
                commentService.editPostComment(userDetails.getUsername(), postId, commentId, commentRequestDTO);

        return ApiResponseDto.from(postCommentResponseDTO);
    }

    @Operation(
            summary = "댓글 삭제 API",
            description = "댓글을 삭제하는 API"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "S000",
                    description = "댓글 삭제 성공"),
    })
    @PatchMapping("/comment/{postId}/{commentId}/delete")
    public ApiResponseDto<Long> deletePostComment(
            @PathVariable(name = "postId") Long postId,
            @PathVariable(name = "commentId") Long commentId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        Long deletedCommentId =
                commentService.removeComment(userDetails.getUsername(), postId, commentId);

        return ApiResponseDto.from(deletedCommentId);
    }



}
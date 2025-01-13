package com.elice.aurasphere.contents.controller;


import com.elice.aurasphere.contents.dto.PostCreateDTO;
import com.elice.aurasphere.contents.dto.PostResDTO;
import com.elice.aurasphere.contents.service.PostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Post", description = "게시글 API")
@Slf4j
@RequestMapping("/post")
@RestController
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) { this.postService = postService; }


    //글 작성 api
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping
    public ResponseEntity<PostResDTO> createPost(
            @Valid @RequestBody PostCreateDTO postCreateDTO
    ){

//        PostResDTO post = postService.processPost(userDetails.getUsername(), postCreateDTO);

        PostResDTO postResDTO = postService.registerPost(postCreateDTO);

        return ResponseEntity.ok(postResDTO);
    }

}

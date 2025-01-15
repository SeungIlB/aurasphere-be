package com.elice.aurasphere.contents.service;


import com.elice.aurasphere.contents.dto.PostCreateDTO;
import com.elice.aurasphere.contents.dto.PostResDTO;
import com.elice.aurasphere.contents.dto.PostUpdateDTO;
import com.elice.aurasphere.contents.entity.Post;
import com.elice.aurasphere.contents.mapper.PostMapper;
import com.elice.aurasphere.contents.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PostService {

    private final PostRepository postRepository;


    private final PostMapper mapper;


    public PostService(PostRepository postRepository, PostMapper mapper) {
        this.postRepository = postRepository;
        this.mapper = mapper;
    }

    //상세 게시글 조회
    public PostResDTO getPost(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NullPointerException("게시글이 존재하지 않습니다."));

        PostResDTO postResDTO = mapper.postToPostResDto(post);

        return mapper.postToPostResDto(post);
    }


    //게시글 생성
    public PostResDTO registerPost(PostCreateDTO postCreateDTO) {

        //게시글 생성
        Post post = Post.builder()
                .content(postCreateDTO.getContent())
                .likeCnt(0L)
                .build();

        Post registeredPost = postRepository.save(post);

        return PostResDTO.builder()
                .id(registeredPost.getId())
                .content(registeredPost.getContent())
                .likeCnt(registeredPost.getLikeCnt())
                .commentCnt(0L)
                .createdAt(registeredPost.getCreatedAt())
                .updatedAt(registeredPost.getUpdatedAt())
                .build();
    }

    //게시글 수정
    public PostResDTO editPost(Long postId, PostUpdateDTO postUpdateDTO) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NullPointerException("게시글이 존재하지 않습니다."));

        return postRepository.findById(postId)
                .map(existingPost -> {

                    existingPost.updatePost(postUpdateDTO.getContent());

                    Post updatedPost = postRepository.save(existingPost);

                    /*
                    댓글 개수 로직 추가해야 함!!!!!!!
                    */
                    return mapper.postToPostResDto(updatedPost);
                })
                .orElseThrow();
    }
}

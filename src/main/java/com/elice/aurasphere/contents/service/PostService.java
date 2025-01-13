package com.elice.aurasphere.contents.service;


import com.elice.aurasphere.contents.dto.PostCreateDTO;
import com.elice.aurasphere.contents.dto.PostResDTO;
import com.elice.aurasphere.contents.entity.Post;
import com.elice.aurasphere.contents.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PostService {

    private final PostRepository postRepository;


    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
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
                .likes(registeredPost.getLikeCnt())
                .commentCnt(0L)
                .createAt(registeredPost.getCreatedAt())
                .updateAt(registeredPost.getUpdatedAt())
                .build();
    }
}

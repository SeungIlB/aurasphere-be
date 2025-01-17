package com.elice.aurasphere.contents.service;


import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.elice.aurasphere.contents.dto.PostCreateDTO;
import com.elice.aurasphere.contents.dto.PostResDTO;
import com.elice.aurasphere.contents.dto.PostUpdateDTO;
import com.elice.aurasphere.contents.entity.Image;
import com.elice.aurasphere.contents.entity.Post;
import com.elice.aurasphere.contents.mapper.PostMapper;
import com.elice.aurasphere.contents.repository.ImageRepository;
import com.elice.aurasphere.contents.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PostService {

    private final PostRepository postRepository;
    private final ImageRepository imageRepository;
    private final S3Service s3Service;


    private final PostMapper mapper;


    public PostService(
            PostRepository postRepository,
            ImageRepository imageRepository,
            S3Service s3Service,
            PostMapper mapper) {

        this.postRepository = postRepository;
        this.imageRepository = imageRepository;
        this.s3Service = s3Service;
        this.mapper = mapper;

    }

    //상세 게시글 조회
    public PostResDTO getPost(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NullPointerException("게시글이 존재하지 않습니다."));

        PostResDTO postResDTO = mapper.postToPostResDto(post);

        return mapper.postToPostResDto(post);
    }


    @Transactional
    //게시글 생성
    public PostResDTO registerPost(PostCreateDTO postCreateDTO) {

        Post registeredPost = postRepository.save(
                Post.builder()
                .content(postCreateDTO.getContent())
                .likeCnt(0L)
                .build()
        );

        List<String> imgUrls = new ArrayList<>();

        //읽어오기용 presigned url 생성하기
        for(String key : postCreateDTO.getImgKeys()){

            String url = s3Service.getGetS3Url(key);

            imgUrls.add(url);

            imageRepository.save(
                    Image.builder()
                            .post(registeredPost)
                            .imgUrl(url)
                            .build());
        }

        return PostResDTO.builder()
                .id(registeredPost.getId())
                .content(registeredPost.getContent())
                .likeCnt(registeredPost.getLikeCnt())
                .commentCnt(0L)
                .imgUrls(imgUrls)
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

                    return mapper.postToPostResDto(updatedPost);
                })
                .orElseThrow();
    }
}

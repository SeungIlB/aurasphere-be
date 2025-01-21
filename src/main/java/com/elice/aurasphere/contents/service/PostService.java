package com.elice.aurasphere.contents.service;


import com.elice.aurasphere.contents.dto.PostCreateDTO;
import com.elice.aurasphere.contents.dto.PostResDTO;
import com.elice.aurasphere.contents.dto.PostUpdateDTO;
import com.elice.aurasphere.contents.entity.Image;
import com.elice.aurasphere.contents.entity.Like;
import com.elice.aurasphere.contents.entity.Post;
import com.elice.aurasphere.contents.mapper.PostMapper;
import com.elice.aurasphere.contents.repository.ImageRepository;
import com.elice.aurasphere.contents.repository.LikeRepository;
import com.elice.aurasphere.contents.repository.PostRepository;
import com.elice.aurasphere.global.exception.CustomException;
import com.elice.aurasphere.global.exception.ErrorCode;
import com.elice.aurasphere.user.entity.User;
import com.elice.aurasphere.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ImageRepository imageRepository;
    private final LikeService likeService;
    private final S3Service s3Service;


    private final PostMapper mapper;


    public PostService(
            UserRepository userRepository,
            PostRepository postRepository,
            ImageRepository imageRepository,
            LikeService likeService,
            S3Service s3Service,
            PostMapper mapper) {

        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.likeService = likeService;
        this.imageRepository = imageRepository;
        this.s3Service = s3Service;
        this.mapper = mapper;

    }

    //상세 게시글 조회
    public PostResDTO getPost(String username, Long postId) {

        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        PostResDTO postResDTO = mapper.postToPostResDto(post);

        //아직 좋아요를 누르기 전이라면 false, 눌렀으면 true 반환
        postResDTO.setLiked(!likeService.isNotAlreadyLike(user, post));

        return mapper.postToPostResDto(post);
    }


    @Transactional
    //게시글 생성
    public PostResDTO registerPost(String username, PostCreateDTO postCreateDTO) {

        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post registeredPost = postRepository.save(
                Post.builder()
                        .user(user)
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
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        return postRepository.findById(postId)
                .map(existingPost -> {

                    existingPost.updatePost(postUpdateDTO.getContent());

                    Post updatedPost = postRepository.save(existingPost);

                    return mapper.postToPostResDto(updatedPost);
                })
                .orElseThrow(() -> new CustomException(ErrorCode.POST_UPDATE_FAILED));
    }
}
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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




    public List<PostResDTO> getMyPosts(String username, Pageable pageable, Long cursor){

        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Post> postList = postRepository.findMyPosts(user.getId(), pageable, cursor);

        log.info("postList : {}", postList.stream().toArray());

        return null;
    }

    //상세 게시글 조회
    public PostResDTO getPost(String username, Long postId) {

        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        List<String> image = imageRepository.findImagesByPostId(post.getId());

        return PostResDTO.builder()
                .id(post.getId())
                .content(post.getContent())
                .likeCnt(post.getLikeCnt())
                .isLiked(!likeService.isNotAlreadyLike(user,post))
                .imgUrls(image)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
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
    public PostResDTO editPost(String username, Long postId, PostUpdateDTO postUpdateDTO) {

        //유저를 찾을 수 없는 경우
        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        //Post를 찾을 수 없는 경우
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        //User와 Post 작성자가 일치하지 않는 경우
        if(!post.getUser().getEmail().equals(user.getEmail()))
            throw new CustomException(ErrorCode.USER_NOT_MATCH);

        return postRepository.findById(postId)
                .map(existingPost -> {

                    existingPost.updatePost(postUpdateDTO.getContent());

                    Post updatedPost = postRepository.save(existingPost);

                    return mapper.postToPostResDto(updatedPost);
                })
                .orElseThrow(() -> new CustomException(ErrorCode.POST_UPDATE_FAILED));
    }
}
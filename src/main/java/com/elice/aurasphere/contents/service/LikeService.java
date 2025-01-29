package com.elice.aurasphere.contents.service;


import com.elice.aurasphere.contents.entity.Like;
import com.elice.aurasphere.contents.entity.Post;
import com.elice.aurasphere.contents.mapper.PostMapper;
import com.elice.aurasphere.contents.repository.LikeRepository;
import com.elice.aurasphere.contents.repository.PostRepository;
import com.elice.aurasphere.global.exception.CustomException;
import com.elice.aurasphere.global.exception.ErrorCode;
import com.elice.aurasphere.user.entity.User;
import com.elice.aurasphere.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final PostMapper mapper;

    public LikeService(
            LikeRepository likeRepository,
            PostRepository postRepository,
            UserRepository userRepository,
            PostMapper mapper) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }


    @Transactional
    public boolean toggleLike(String username, Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        //좋아요 누른 적이 없다면 좋아요 눌러서 저장
        if(isNotAlreadyLike(user, post)) {
            likeRepository.save(
                    Like.builder()
                            .user(user)
                            .post(post)
                            .build()
            );
//            postRepository.findById(postId)
//                    .map(existingPost -> {
//
//                        existingPost.addLike();
//
//                        Post updatedPost = postRepository.save(existingPost);
//
//                        return mapper.postToPostResDto(updatedPost);
//                    })
//                    .orElseThrow(() -> new CustomException(ErrorCode.POST_UPDATE_FAILED));
            return true;
        }else {
            likeRepository.deleteLikeByUserAndPost(user, post);
//            postRepository.findById(postId)
//                    .map(existingPost -> {
//
//                        existingPost.removeLike();
//
//                        Post updatedPost = postRepository.save(existingPost);
//
//                        return mapper.postToPostResDto(updatedPost);
//                    })
//                    .orElseThrow(() -> new CustomException(ErrorCode.POST_UPDATE_FAILED));
        }



        //좋아요 누른 적이 있으면 false 반환
        return false;
    }

    //해당 user가 post를 좋아요 누른 적 있는 지 확인
    public boolean isNotAlreadyLike(User user, Post post) {
        return likeRepository.findByUserAndPost(user, post).isEmpty();
    }

    public Long getLikeCnt(Long postId){
        return likeRepository.findlikeCntByPostId(postId);
    }



}

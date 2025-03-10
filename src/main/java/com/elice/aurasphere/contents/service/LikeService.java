package com.elice.aurasphere.contents.service;


import com.elice.aurasphere.contents.entity.Like;
import com.elice.aurasphere.contents.entity.Post;
import com.elice.aurasphere.contents.repository.LikeRepository;
import com.elice.aurasphere.contents.repository.PostRepository;
import com.elice.aurasphere.global.exception.CustomException;
import com.elice.aurasphere.global.exception.ErrorCode;
import com.elice.aurasphere.notification.dto.NotificationType;
import com.elice.aurasphere.notification.service.NotificationService;
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
    private final NotificationService notificationService; // 알림 서비스 추가


    public LikeService(
            LikeRepository likeRepository,
            PostRepository postRepository,
            UserRepository userRepository, NotificationService notificationService) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
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
            return true;
        }else {
            likeRepository.deleteLikeByUserAndPost(user, post);
        }

        if (!user.equals(post.getUser())) { // 자기 자신에게 알림 보내지 않도록 체크
            notificationService.createNotification(
                    user,
                    post.getUser(),
                    NotificationType.LIKE
            );
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

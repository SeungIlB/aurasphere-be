package com.elice.aurasphere.contents.service;


import com.elice.aurasphere.contents.dto.CommentListResDTO;
import com.elice.aurasphere.contents.dto.CommentReqDTO;
import com.elice.aurasphere.contents.dto.CommentResDTO;
import com.elice.aurasphere.contents.dto.PostReqDTO;
import com.elice.aurasphere.contents.entity.Comment;
import com.elice.aurasphere.contents.entity.Post;
import com.elice.aurasphere.contents.repository.CommentRepository;
import com.elice.aurasphere.contents.repository.PostRepository;
import com.elice.aurasphere.global.exception.CustomException;
import com.elice.aurasphere.global.exception.ErrorCode;
import com.elice.aurasphere.user.entity.Profile;
import com.elice.aurasphere.user.entity.User;
import com.elice.aurasphere.user.repository.ProfileRepository;
import com.elice.aurasphere.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ProfileRepository profileRepository;

    public CommentService(
            UserRepository userRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            ProfileRepository profileRepository
    ) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.profileRepository = profileRepository;
    }

    public CommentListResDTO getCommentList(String username, Long postId, int size, Long cursor) {

        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
               .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        List<Comment> commentList = commentRepository.findCommentsByPostId(postId, size, cursor);

        if(commentList.isEmpty()){
            return CommentListResDTO.builder().hasNext(false).build();
        }

        List<CommentResDTO> comments = commentList.stream().map(comment -> {

            Profile profile = profileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

            return CommentResDTO.builder()
                    .id(comment.getId())
                    .nickname(profile.getNickname())
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .updatedAt(comment.getUpdatedAt())
                    .build();
        }).toList();

        Long lastCursor = commentList.get(commentList.size() - 1).getId();
        boolean hasNext = commentList.size() >= size;

        return CommentListResDTO.builder()
                .commentList(comments)
                .comment_cursor(lastCursor)
                .hasNext(hasNext)
                .build();
    }


    public CommentResDTO createPostComment(String username, Long postId, CommentReqDTO postReqDTO) {

        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .content(postReqDTO.getContent())
                .build();

        commentRepository.save(comment);

        return CommentResDTO.builder()
                .id(comment.getId())
               .nickname(profile.getNickname())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
               .build();
    }
}
package com.elice.aurasphere.contents.service;


import com.elice.aurasphere.contents.dto.CommentListResponseDTO;
import com.elice.aurasphere.contents.dto.CommentRequestDTO;
import com.elice.aurasphere.contents.dto.CommentResponseDTO;
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

    public CommentListResponseDTO getCommentList(String username, Long postId, int size, Long cursor) {

        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
               .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        List<Comment> commentList = commentRepository.findCommentsByPostId(postId, size, cursor);

        if(commentList.isEmpty()){
            return CommentListResponseDTO.builder().hasNext(false).build();
        }

        List<CommentResponseDTO> comments = commentList.stream().map(comment -> {

            Profile profile = profileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

            return CommentResponseDTO.builder()
                    .id(comment.getId())
                    .nickname(profile.getNickname())
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .updatedAt(comment.getUpdatedAt())
                    .build();
        }).toList();

        Long lastCursor = commentList.get(commentList.size() - 1).getId();
        boolean hasNext = commentList.size() >= size;

        return CommentListResponseDTO.builder()
                .commentList(comments)
                .comment_cursor(lastCursor)
                .hasNext(hasNext)
                .build();
    }


    public CommentResponseDTO createPostComment(String username, Long postId, CommentRequestDTO postReqDTO) {

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

        return CommentResponseDTO.builder()
                .id(comment.getId())
               .nickname(profile.getNickname())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
               .build();
    }

    public CommentResponseDTO editPostComment(String username, Long postId, Long commentId, CommentRequestDTO commentReqDTO) {
        User user = userRepository.findByEmail(username)
               .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
               .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Comment comment = commentRepository.findById(commentId)
               .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if(!comment.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.USER_NOT_MATCH);
        }

        comment.updateComment(commentReqDTO.getContent());
        commentRepository.save(comment);

        return CommentResponseDTO.builder()
               .id(comment.getId())
               .nickname(comment.getUser().getProfile().getNickname())
               .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
               .updatedAt(comment.getUpdatedAt())
                .build();
    }

    public Long removeComment(String username, Long postId, Long commentId) {

        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if(!comment.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.USER_NOT_MATCH);
        }

        comment.removeComment();

        commentRepository.save(comment);

        return comment.getId();

    }
}
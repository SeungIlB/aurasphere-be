package com.elice.aurasphere.contents.repository;

import com.elice.aurasphere.contents.entity.Comment;
import com.elice.aurasphere.contents.entity.Post;

import java.util.List;

public interface CommentCustomRepository {

    List<Comment> findCommentsByPostId(Long postId, int size, Long cursor);

}

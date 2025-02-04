package com.elice.aurasphere.contents.repository;

import com.elice.aurasphere.contents.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentCustomRepository {
}

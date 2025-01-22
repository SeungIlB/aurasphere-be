package com.elice.aurasphere.contents.repository;

import com.elice.aurasphere.contents.entity.Like;
import com.elice.aurasphere.contents.entity.Post;
import com.elice.aurasphere.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserAndPost(User user, Post post);

    Optional<Like> deleteLikeByUserAndPost(User user, Post post);
}

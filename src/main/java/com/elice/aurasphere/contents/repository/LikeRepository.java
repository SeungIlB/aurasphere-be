package com.elice.aurasphere.contents.repository;

import com.elice.aurasphere.contents.entity.Like;
import com.elice.aurasphere.contents.entity.Post;
import com.elice.aurasphere.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long>, LikeCustomRepository {

    Optional<Like> findByUserAndPost(User user, Post post);

    Optional<Like> deleteLikeByUserAndPost(User user, Post post);
}

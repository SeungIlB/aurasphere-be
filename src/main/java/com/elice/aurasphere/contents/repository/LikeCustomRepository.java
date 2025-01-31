package com.elice.aurasphere.contents.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface LikeCustomRepository {

    public Long findlikeCntByPostId(Long postId);
}

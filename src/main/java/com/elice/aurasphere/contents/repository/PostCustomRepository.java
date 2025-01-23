package com.elice.aurasphere.contents.repository;

import com.elice.aurasphere.contents.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostCustomRepository {

    public List<Post> findAllPostsByAsc(Long userId, int size, Long cursor);

    List<Post> findMyPosts(Long userId, int size, Long cursor);

    List<Post> findPostsByLikes(Long userId, int size, Long cursor);

}

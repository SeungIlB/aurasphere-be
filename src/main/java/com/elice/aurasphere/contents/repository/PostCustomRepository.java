package com.elice.aurasphere.contents.repository;

import com.elice.aurasphere.contents.dto.FilterResDTO;
import com.elice.aurasphere.contents.entity.Post;
import com.querydsl.core.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PostCustomRepository {

    public FilterResDTO findAllPostsByAsc(Long userId, int size, Long filter_cursor);

    List<Post> findMyPosts(Long userId, int size, Long cursor);

    FilterResDTO findPostsByLikes(Long userId, int size, Long post_cursor, Optional<Long> filter_cursor);

}

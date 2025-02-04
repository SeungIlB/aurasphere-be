package com.elice.aurasphere.contents.repository;

import com.elice.aurasphere.contents.dto.FilterResDTO;
import com.elice.aurasphere.contents.entity.Post;
import com.querydsl.core.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostCustomRepository {

    public FilterResDTO findAllPostsByAsc(Long userId, int size, Long postCursor);

    List<Post> findMyPosts(Long userId, int size, Long cursor);

    FilterResDTO findPostsByLikes(Long userId, int size, Long postCursor, Optional<Long> filterCursor);

    FilterResDTO findAllPostsByViews(Long userId, int size, Long postCursor, Optional<Long> filterCursor);
    FilterResDTO findAllPostsByFollowing(Long userId, int size, Long postCursor, Optional<Long> filterCursor);
    Long countByUserIdAndDeletedDateIsNull(Long userId);
}

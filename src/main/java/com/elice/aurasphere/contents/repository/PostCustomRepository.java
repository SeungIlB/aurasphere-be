package com.elice.aurasphere.contents.repository;

import com.elice.aurasphere.contents.dto.FilterResponseDTO;
import com.elice.aurasphere.contents.entity.Post;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostCustomRepository {

    public FilterResponseDTO findAllPostsByAsc(Long userId, int size, Long postCursor);

    List<Post> findMyPosts(Long userId, int size, Long cursor);

    FilterResponseDTO findPostsByLikes(Long userId, int size, Long postCursor, Optional<Long> filterCursor);

    FilterResponseDTO findAllPostsByViews(Long userId, int size, Long postCursor, Optional<Long> filterCursor);
    FilterResponseDTO findAllPostsByFollowing(Long userId, int size, Long postCursor, Optional<Long> filterCursor);
    Long countByUserIdAndDeletedDateIsNull(Long userId);
}

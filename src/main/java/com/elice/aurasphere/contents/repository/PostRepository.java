package com.elice.aurasphere.contents.repository;

import com.elice.aurasphere.contents.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostCustomRepository {
    Page<Post> findByDeletedDateIsNull(Pageable pageable);

}

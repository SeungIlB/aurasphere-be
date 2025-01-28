package com.elice.aurasphere.contents.repository;

import com.elice.aurasphere.contents.entity.View;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ViewRepository extends JpaRepository<View, Long> {

    public Optional<View> findByPostId(Long postId);

    public Long findViewCntByPostId(Long postId);
}

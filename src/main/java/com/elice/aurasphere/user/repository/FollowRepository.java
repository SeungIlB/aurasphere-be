package com.elice.aurasphere.user.repository;

import com.elice.aurasphere.user.entity.Follow;
import com.elice.aurasphere.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerAndFollowing(User follower, User following);

    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    Long countByFollower(User user);

    Long countByFollowing(User user);
}
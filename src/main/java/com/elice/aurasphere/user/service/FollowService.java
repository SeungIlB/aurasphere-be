package com.elice.aurasphere.user.service;

import com.elice.aurasphere.global.exception.CustomException;
import com.elice.aurasphere.global.exception.ErrorCode;
import com.elice.aurasphere.user.dto.FollowUserResponseDTO;
import com.elice.aurasphere.user.entity.Follow;
import com.elice.aurasphere.user.entity.User;
import com.elice.aurasphere.user.repository.FollowRepository;
import com.elice.aurasphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public void follow(String followerEmail, Long followingId) {
        User follower = userRepository.findByEmail(followerEmail)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        User following = userRepository.findById(followingId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 자기 자신을 팔로우하는 경우 예외 처리
        if (follower.getId().equals(following.getId())) {
            throw new CustomException(ErrorCode.CANNOT_FOLLOW_YOURSELF);
        }

        // 이미 팔로우 상태라면 아무 작업하지 않고 성공으로 처리
        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            log.info("User {} is already following user {}", followerEmail, followingId);
            return;
        }

        Follow follow = Follow.builder()
            .follower(follower)
            .following(following)
            .build();

        followRepository.save(follow);
    }

    @Transactional
    public void unfollow(String followerEmail, Long followingId) {
        User follower = userRepository.findByEmail(followerEmail)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        User following = userRepository.findById(followingId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 팔로우 관계가 없다면 아무 작업하지 않고 성공으로 처리
        followRepository.findByFollowerAndFollowing(follower, following)
            .ifPresentOrElse(
                followRepository::delete,
                () -> log.info("User {} is not following user {}", followerEmail, followingId)
            );
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(String followerEmail, Long followingId) {
        User follower = userRepository.findByEmail(followerEmail)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        User following = userRepository.findById(followingId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return followRepository.existsByFollowerAndFollowing(follower, following);
    }

    @Transactional(readOnly = true)
    public long getFollowerCount(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return followRepository.countByFollowing(user);
    }

    @Transactional(readOnly = true)
    public long getFollowingCount(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return followRepository.countByFollower(user);
    }

    @Transactional(readOnly = true)
    public List<FollowUserResponseDTO> getFollowers(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return user.getFollowers().stream()
            .map(follow -> FollowUserResponseDTO.from(follow.getFollower()))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FollowUserResponseDTO> getFollowing(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return user.getFollowing().stream()
            .map(follow -> FollowUserResponseDTO.from(follow.getFollowing()))
            .collect(Collectors.toList());
    }
}
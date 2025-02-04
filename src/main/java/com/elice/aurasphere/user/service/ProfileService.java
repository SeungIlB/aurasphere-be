package com.elice.aurasphere.user.service;

import com.elice.aurasphere.contents.repository.PostRepository;
import com.elice.aurasphere.global.s3.service.S3Service;
import com.elice.aurasphere.global.exception.CustomException;
import com.elice.aurasphere.global.exception.ErrorCode;
import com.elice.aurasphere.user.dto.ProfileRequestDTO;
import com.elice.aurasphere.user.dto.ProfileResponseDTO;
import com.elice.aurasphere.user.entity.Profile;
import com.elice.aurasphere.user.entity.User;
import com.elice.aurasphere.user.repository.FollowRepository;
import com.elice.aurasphere.user.repository.ProfileRepository;
import com.elice.aurasphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final S3Service s3Service;
    private static final String DEFAULT_PROFILE_URL = "DEFAULT";

    @Transactional
    public ProfileResponseDTO getProfile(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 게시글 수 조회
        Long postsCount = postRepository.countByUserIdAndDeletedDateIsNull(userId);

        // 팔로워/팔로잉 수 조회
        Long followerCount = followRepository.countByFollowing(profile.getUser());
        Long followingCount = followRepository.countByFollower(profile.getUser());


        return ProfileResponseDTO.builder()
            .nickname(profile.getNickname())
            .profileUrl(profile.getProfileUrl())
            .postsCount(postsCount)
            .followerCount(followerCount)
            .followingCount(followingCount)
            .build();
    }

    @Transactional
    public ProfileResponseDTO updateProfile(Long userId, String nickname, MultipartFile file) throws IOException {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (nickname != null) {
            if (profileRepository.existsByNicknameAndUserIdNot(nickname, userId)) {
                throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
            }
            profile.updateProfileNickname(nickname);
        }

        if (file != null) {
            String imageUrl = s3Service.uploadFile(file, "profile");
            profile.updateProfileUrl(imageUrl);
        }

        profileRepository.save(profile);

        return ProfileResponseDTO.builder()
                .nickname(profile.getNickname())
                .profileUrl(profile.getProfileUrl()) // "DEFAULT" 또는 실제 S3 URL
                .build();
    }

//    @Transactional
//    public ProfileResponseDTO updateProfile(Long userId, ProfileRequestDTO request) {
//        Profile profile = profileRepository.findByUserId(userId)
//            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
//
//        if (request.getNickname() != null) {
//            if (profileRepository.existsByNicknameAndUserIdNot(request.getNickname(), userId)) {
//                throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
//            }
//            profile.updateProfileNickname(request.getNickname());
//        }
//
//        if (request.getImageKey() != null) {
//            String imageUrl = s3Service.getGetS3Url(request.getImageKey());
//            profile.updateProfileUrl(imageUrl);
//        }
//
//        profileRepository.save(profile);
//
//        return ProfileResponseDTO.builder()
//            .nickname(profile.getNickname())
//            .profileUrl(profile.getProfileUrl()) // "DEFAULT" 또는 실제 S3 URL
//            .build();
//    }



    @Transactional
    public void updateProfileImageUrl(Long userId, String newImageUrl) {
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (isKakaoImageUrl(newImageUrl)) {
            // Kakao 이미지 URL인 경우 만료 시간 정보 저장
            profile.updateProfileUrlWithExpiry(newImageUrl, LocalDateTime.now().plusDays(30));
        } else {
            profile.updateProfileUrl(newImageUrl);
        }
    }

    private boolean isKakaoImageUrl(String imageUrl) {
        return imageUrl != null && imageUrl.contains("kakao.co.kr");
    }

    // 매일 자정에 실행되는 스케줄러
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void checkAndUpdateExpiredImageUrls() {
        List<Profile> profilesWithExpiredUrls = profileRepository.findAllByProfileUrlExpiryDateBefore(
            LocalDateTime.now());

        for (Profile profile : profilesWithExpiredUrls) {
            profile.updateProfileUrl(DEFAULT_PROFILE_URL);
            log.info("Updated expired profile image URL for user: {}", profile.getUser().getId());
        }
    }
}
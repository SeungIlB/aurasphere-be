package com.elice.aurasphere.user.service;

import com.elice.aurasphere.contents.service.S3Service;
import com.elice.aurasphere.global.exception.CustomException;
import com.elice.aurasphere.global.exception.ErrorCode;
import com.elice.aurasphere.user.dto.ProfileRequest;
import com.elice.aurasphere.user.dto.ProfileResponse;
import com.elice.aurasphere.user.entity.Profile;
import com.elice.aurasphere.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final S3Service s3Service;
    private static final String DEFAULT_PROFILE_URL = "DEFAULT";

    public ProfileResponse getProfile(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return ProfileResponse.builder()
            .nickname(profile.getNickname())
            .profileUrl(profile.getProfileUrl())
            .build();
    }

    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (request.getNickname() != null) {
            if (profileRepository.existsByNicknameAndUserIdNot(request.getNickname(), userId)) {
                throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
            }
            profile.updateProfileNickname(request.getNickname());
        }

        if (request.getImageKey() != null) {
            String imageUrl = s3Service.getGetS3Url(request.getImageKey());
            profile.updateProfileUrl(imageUrl);
        }

        profileRepository.save(profile);

        return ProfileResponse.builder()
            .nickname(profile.getNickname())
            .profileUrl(profile.getProfileUrl()) // "DEFAULT" 또는 실제 S3 URL
            .build();
    }

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
package com.elice.aurasphere.config;

import com.elice.aurasphere.config.CustomUserDetails;
import com.elice.aurasphere.user.entity.Profile;
import com.elice.aurasphere.user.entity.User;
import com.elice.aurasphere.user.repository.ProfileRepository;
import com.elice.aurasphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oauth2UserInfo = getOAuth2UserInfo(provider, oauth2User.getAttributes());

        Optional<User> userOptional = userRepository.findByEmail(oauth2UserInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            updateExistingUser(user, oauth2UserInfo);
        } else {
            user = registerNewUser(userRequest, oauth2UserInfo);
        }

        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
            oauth2User.getAttributes(),
            "email"  // 또는 provider별 nameAttributeKey 설정
        );
    }

    private OAuth2UserInfo getOAuth2UserInfo(String provider, Map<String, Object> attributes) {
        if (provider.equals("naver")) {
            return new NaverOAuth2UserInfo(attributes);
        } else if (provider.equals("kakao")) {
            return new KakaoOAuth2UserInfo(attributes);
        }
        throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
    }

    private User registerNewUser(OAuth2UserRequest userRequest, OAuth2UserInfo oauth2UserInfo) {
        // 이메일이 없는 경우 처리
        String email = oauth2UserInfo.getEmail();
        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        User user = User.builder()
            .name(oauth2UserInfo.getName() != null ? oauth2UserInfo.getName() : "Unknown")
            .email(email)
            .role("USER")
            .password("")
            .build();

        user = userRepository.save(user);

        // getNickname()과 getImageUrl()은 이제 null을 반환하지 않음 (기본값 사용)
        Profile profile = Profile.builder()
            .nickname(oauth2UserInfo.getNickname())
            .profileUrl(oauth2UserInfo.getImageUrl())
            .build();

        user.addProfile(profile);
        profileRepository.save(profile);

        return user;
    }

    private void updateExistingUser(User user, OAuth2UserInfo oauth2UserInfo) {
        Profile profile = user.getProfile();
        if (profile != null) {
            String currentNickname = profile.getNickname();
            String currentImageUrl = profile.getProfileUrl();

            // 닉네임 업데이트 여부 확인
            String rawNickname = oauth2UserInfo.getRawNickname();
            boolean shouldUpdateNickname = rawNickname != null &&
                currentNickname.startsWith(oauth2UserInfo.provider);

            // 프로필 이미지 업데이트 여부 확인
            String rawImageUrl = oauth2UserInfo.getRawImageUrl();
            boolean shouldUpdateImage = rawImageUrl != null &&
                currentImageUrl.equals("/images/default_profile.png");

            // 업데이트가 필요한 경우에만 새 값 사용, 아니면 기존 값 유지
            String newNickname = shouldUpdateNickname ? rawNickname : currentNickname;
            String newImageUrl = shouldUpdateImage ? rawImageUrl : currentImageUrl;

            // 어느 하나라도 업데이트가 필요한 경우에만 프로필 업데이트
            if (shouldUpdateNickname || shouldUpdateImage) {
                profile.updateProfile(newNickname, newImageUrl);
            }
        } else {
            // 프로필이 없는 경우 (비정상 케이스) 새로 생성
            Profile newProfile = Profile.builder()
                .nickname(oauth2UserInfo.getNickname())
                .profileUrl(oauth2UserInfo.getImageUrl())
                .build();
            user.addProfile(newProfile);
            profileRepository.save(newProfile);
        }
    }
}
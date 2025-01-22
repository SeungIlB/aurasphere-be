package com.elice.aurasphere.user.service;

import com.elice.aurasphere.global.exception.CustomException;
import com.elice.aurasphere.global.exception.ErrorCode;
import com.elice.aurasphere.user.entity.CustomUserDetails;
import com.elice.aurasphere.user.entity.Profile;
import com.elice.aurasphere.user.entity.User;
import com.elice.aurasphere.user.repository.ProfileRepository;
import com.elice.aurasphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;  // Profile 레포지토리 추가

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Attempting to load user: {}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        log.info("User found: {}", user.getEmail());
        log.info("User found: {}", user.getId());

        Profile profile = profileRepository.findByUserId(user.getId())
            .orElse(null);

        if (profile != null) {
            log.info("Profile found: {}", profile.getNickname());
        } else {
            log.warn("No profile found for user: {}", email);
        }

        return CustomUserDetails.from(user, profile);
    }
}
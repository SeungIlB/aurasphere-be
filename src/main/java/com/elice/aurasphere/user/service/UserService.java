package com.elice.aurasphere.user.service;

import com.elice.aurasphere.config.utils.CookieUtil;
import com.elice.aurasphere.config.authentication.JwtTokenProvider;
import com.elice.aurasphere.global.exception.CustomException;
import com.elice.aurasphere.global.exception.ErrorCode;
import com.elice.aurasphere.user.dto.LoginRequest;
import com.elice.aurasphere.user.dto.SignupRequest;
import com.elice.aurasphere.user.entity.Profile;
import com.elice.aurasphere.user.entity.User;
import com.elice.aurasphere.user.repository.ProfileRepository;
import com.elice.aurasphere.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final CookieUtil cookieUtil;

    public void login(LoginRequest loginRequest, HttpServletResponse response) {
        // 인증
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        // 권한 정보 추출
        List<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        // 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(authentication.getName(), roles);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication.getName());

        // 쿠키에 토큰 추가
        cookieUtil.addAccessTokenCookie(response, accessToken, jwtTokenProvider.REFRESH_TOKEN_VALIDITY);
        cookieUtil.addRefreshTokenCookie(response, refreshToken, jwtTokenProvider.REFRESH_TOKEN_VALIDITY);
    }

    public User signup(SignupRequest signupRequest) {
        // 이메일 중복 체크
        if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 닉네임 중복 체크
        if (profileRepository.existsByNickname(signupRequest.getNickname())) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        // 사용자 생성
        User user = User.builder()
            .email(signupRequest.getEmail())
            .password(passwordEncoder.encode(signupRequest.getPassword()))
            .role("USER") // 기본 역할 설정
            .build();

        // 프로필 생성
        Profile profile = Profile.builder()
            .user(user)
            .nickname(signupRequest.getNickname())
            .profileUrl("DEFAULT") // 기본 프로필 이미지 경로
            .build();

        // 사용자 저장
        User savedUser = userRepository.save(user);
        profileRepository.save(profile);

        return savedUser;
    }

    public void logout(HttpServletResponse response) {
        log.info("Logout process started");
        cookieUtil.deleteAccessTokenCookie(response);
        log.info("Access token cookie deleted");
        cookieUtil.deleteRefreshTokenCookie(response);
        log.info("Refresh token cookie deleted");
        log.info("Logout completed successfully");
    }

    public boolean checkEmailDuplication(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean checkNicknameDuplication(String nickname) {
        return profileRepository.existsByNickname(nickname);
    }
}

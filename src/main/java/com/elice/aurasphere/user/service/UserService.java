package com.elice.aurasphere.user.service;

import com.elice.aurasphere.global.utils.CookieUtil;
import com.elice.aurasphere.global.authentication.JwtTokenProvider;
import com.elice.aurasphere.global.exception.CustomException;
import com.elice.aurasphere.global.exception.ErrorCode;
import com.elice.aurasphere.user.dto.LoginRequestDTO;
import com.elice.aurasphere.user.dto.PasswordUpdateRequestDTO;
import com.elice.aurasphere.user.dto.SignupRequestDTO;
import com.elice.aurasphere.user.dto.TokenInfoDTO;
import com.elice.aurasphere.user.entity.Profile;
import com.elice.aurasphere.user.entity.User;
import com.elice.aurasphere.user.repository.ProfileRepository;
import com.elice.aurasphere.user.repository.RefreshTokenRepository;
import com.elice.aurasphere.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final CookieUtil cookieUtil;

    @Value("${default.profile.image.url}")
    private String defaultProfileImageUrl;

    public TokenInfoDTO login(LoginRequestDTO loginRequest) {
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

        // TokenInfo로 반환
        return new TokenInfoDTO(
            "Bearer",
            accessToken,
            refreshToken,
            jwtTokenProvider.REFRESH_TOKEN_VALIDITY
        );
//        // 쿠키에 토큰 추가
//        cookieUtil.addAccessTokenCookie(response, accessToken, jwtTokenProvider.REFRESH_TOKEN_VALIDITY);
//        cookieUtil.addRefreshTokenCookie(response, refreshToken, jwtTokenProvider.REFRESH_TOKEN_VALIDITY);
    }

    @Transactional
    public void signup(SignupRequestDTO signupRequest) {
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
            .profileUrl(defaultProfileImageUrl) // 기본 프로필 이미지 경로
            .build();

        // 사용자 저장
        userRepository.save(user);
        profileRepository.save(profile);
    }

//    public void logout(HttpServletResponse response) {
//        log.info("Logout process started");
//        cookieUtil.deleteAccessTokenCookie(response);
//        log.info("Access token cookie deleted");
//        cookieUtil.deleteRefreshTokenCookie(response);
//        log.info("Refresh token cookie deleted");
//        log.info("Logout completed successfully");
//    }

    @Transactional
    public void logout(String email) {
        log.info("Logout process started for user: {}", email);
        refreshTokenRepository.deleteByUsername(email);
        log.info("Refresh token deleted and logout completed for user: {}", email);
    }

    public boolean checkEmailDuplication(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean checkNicknameDuplication(String nickname) {
        return profileRepository.existsByNickname(nickname);
    }

    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.updatePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void updatePassword(String email, PasswordUpdateRequestDTO request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.CURRENT_PASSWORD_NOT_MATCH);
        }

        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new CustomException(ErrorCode.NEW_PASSWORD_NOT_MATCH);
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // Refresh 토큰 삭제
        refreshTokenRepository.deleteByUsername(email);

        // 프로필 삭제
        profileRepository.deleteByUserId(user.getId());

        // 유저 삭제
        userRepository.delete(user);
    }
}

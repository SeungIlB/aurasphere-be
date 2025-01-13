package com.elice.aurasphere.user.service;

import com.elice.aurasphere.config.JwtTokenProvider;
import com.elice.aurasphere.user.dto.LoginRequest;
import com.elice.aurasphere.user.dto.TokenResponse;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponse login(LoginRequest loginRequest) {

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

        return new TokenResponse("Bearer", accessToken, refreshToken, jwtTokenProvider.ACCESS_TOKEN_VALIDITY);
    }
}

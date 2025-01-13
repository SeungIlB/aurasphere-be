package com.elice.aurasphere.user.controller;

import com.elice.aurasphere.config.JwtTokenProvider;
import com.elice.aurasphere.user.dto.LoginRequest;
import com.elice.aurasphere.user.dto.TokenResponse;
import com.elice.aurasphere.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest) {

        TokenResponse tokenResponse = userService.login(loginRequest);

        return ResponseEntity.status(HttpStatus.OK).body(tokenResponse);
    }

//    @PostMapping("/reissue")
//    public ResponseEntity<TokenResponse> reissue(@RequestBody TokenRequest request) {
//        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
//            throw new RuntimeException("Invalid refresh token");
//        }
//
//        String userEmail = jwtTokenProvider.getUserEmail(request.getRefreshToken());
//        Authentication authentication = jwtTokenProvider.getAuthentication(request.getRefreshToken());
//
//        List<String> roles = authentication.getAuthorities().stream()
//            .map(GrantedAuthority::getAuthority)
//            .collect(Collectors.toList());
//
//        String newAccessToken = jwtTokenProvider.createAccessToken(userEmail, roles);
//        String newRefreshToken = jwtTokenProvider.createRefreshToken(userEmail);
//
//        return ResponseEntity.ok(new TokenResponse(
//            "Bearer",
//            newAccessToken,
//            newRefreshToken,
//            jwtTokenProvider.ACCESS_TOKEN_VALIDITY
//        ));
//    }
}


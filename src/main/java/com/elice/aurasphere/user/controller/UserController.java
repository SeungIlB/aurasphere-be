package com.elice.aurasphere.user.controller;

import com.elice.aurasphere.user.dto.EmailCheckRequest;
import com.elice.aurasphere.user.dto.ErrorResponse;
import com.elice.aurasphere.user.dto.LoginRequest;
import com.elice.aurasphere.user.dto.SignupRequest;
import com.elice.aurasphere.user.dto.TokenInfo;
import com.elice.aurasphere.user.service.UserService;
import com.elice.aurasphere.user.entity.User;
import com.elice.aurasphere.config.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @Operation(summary = "로그인", description = "이메일과 비밀번호를 통해 로그인을 진행합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공 - 쿠키에 토큰 저장됨",
            content = @Content(schema = @Schema(implementation = TokenInfo.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<TokenInfo> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        log.info("Login request received for email: {}", loginRequest.getEmail());
        userService.login(loginRequest, response);

        // response header 확인
        Collection<String> headerNames = response.getHeaderNames();
        log.info("Response headers: {}", headerNames);
        headerNames.forEach(headerName -> {
            if (headerName.toLowerCase().contains("cookie")) {
                log.info("Cookie header: {} = {}", headerName, response.getHeader(headerName));
            }
        });

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            User user = userService.signup(signupRequest);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (RuntimeException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        log.info("logout endpoint accessed");
        userService.logout(response);
        return ResponseEntity.ok().build();
    }

    // 인증이 필요한 엔드포인트 예시
    @GetMapping("/user/protected")
    public ResponseEntity<String> protectedEndpoint() {
        log.info("Protected endpoint accessed");  // 여기에 로그 추가
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Current authentication: {}", auth != null ? auth.getName() : "null");
        return ResponseEntity.ok("인증된 사용자만 접근 가능한 엔드포인트");
    }

    @GetMapping("/user/checkEmail")
    public ResponseEntity<Boolean> checkEmailDuplication(@RequestBody EmailCheckRequest request) {
        boolean isDuplicate = userService.checkEmailDuplication(request.getEmail());
        return ResponseEntity.ok(isDuplicate);
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


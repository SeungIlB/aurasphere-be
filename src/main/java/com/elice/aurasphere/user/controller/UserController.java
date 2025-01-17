package com.elice.aurasphere.user.controller;

import com.elice.aurasphere.global.common.ApiRes;
import com.elice.aurasphere.user.dto.EmailCheckRequest;
import com.elice.aurasphere.user.dto.ErrorResponse;
import com.elice.aurasphere.user.dto.LoginRequest;
import com.elice.aurasphere.user.dto.NicknameCheckRequest;
import com.elice.aurasphere.user.dto.SignupRequest;
import org.springframework.validation.BindingResult;
import com.elice.aurasphere.user.service.UserService;
import com.elice.aurasphere.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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

    @Operation(summary = "로그인 API", description = "이메일과 비밀번호로 로그인하는 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = {@Content(schema = @Schema(implementation = ApiRes.class))}),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 - 이메일 형식 오류 또는 필수 값 누락",
            content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호가 일치하지 않습니다.")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiRes<Void>> login(@Valid @RequestBody LoginRequest loginRequest, BindingResult bindingResult, HttpServletResponse response) {
        log.info("Login request received for email: {}", loginRequest.getEmail());

        // Validation 실패 처리
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldError().getDefaultMessage();
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiRes.failureRes(HttpStatus.BAD_REQUEST, errorMessage, null));
        }

        userService.login(loginRequest, response);
        return ResponseEntity.ok(ApiRes.successRes(HttpStatus.OK, null));
    }

    @Operation(summary = "회원가입 API", description = "새로운 사용자를 등록하는 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "회원가입 성공",
            content = {@Content(schema = @Schema(implementation = ApiRes.class))}),
        @ApiResponse(responseCode = "400", description = "회원가입 실패 - 중복된 이메일 또는 닉네임")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiRes<Void>> signup(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            User user = userService.signup(signupRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiRes.successRes(HttpStatus.CREATED, null));
        } catch (RuntimeException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiRes.failureRes(HttpStatus.BAD_REQUEST, e.getMessage(), null));
        }
    }

    @Operation(summary = "로그아웃 API", description = "사용자 로그아웃을 처리하는 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그아웃 성공",
            content = {@Content(schema = @Schema(implementation = ApiRes.class))})
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiRes<Void>> logout(HttpServletResponse response) {
        log.info("logout endpoint accessed");
        userService.logout(response);
        return ResponseEntity.ok(ApiRes.successRes(HttpStatus.OK, null));
    }

//    // 인증이 필요한 엔드포인트 예시
//    @GetMapping("/user/protected")
//    public ResponseEntity<String> protectedEndpoint() {
//        log.info("Protected endpoint accessed");  // 여기에 로그 추가
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        log.info("Current authentication: {}", auth != null ? auth.getName() : "null");
//        return ResponseEntity.ok("인증된 사용자만 접근 가능한 엔드포인트");
//    }

    @Operation(summary = "이메일 중복 확인 API", description = "회원가입 시 이메일 중복을 확인하는 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "True or False",
            content = {@Content(schema = @Schema(implementation = ApiRes.class))})
    })
    @GetMapping("/user/checkEmail")
    public ResponseEntity<ApiRes<Boolean>> checkEmailDuplication(@RequestBody EmailCheckRequest request) {
        boolean isDuplicate = userService.checkEmailDuplication(request.getEmail());
        return ResponseEntity.ok(ApiRes.successRes(HttpStatus.OK, isDuplicate));
    }

    @Operation(summary = "닉네임 중복 확인 API", description = "회원가입 시 닉네임 중복을 확인하는 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "True or False",
            content = {@Content(schema = @Schema(implementation = ApiRes.class))})
    })
    @GetMapping("/user/checkNickname")
    public ResponseEntity<ApiRes<Boolean>> checkNicknameDuplication(@RequestBody NicknameCheckRequest request) {
        boolean isDuplicate = userService.checkNicknameDuplication(request.getNickname());
        return ResponseEntity.ok(ApiRes.successRes(HttpStatus.OK, isDuplicate));
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


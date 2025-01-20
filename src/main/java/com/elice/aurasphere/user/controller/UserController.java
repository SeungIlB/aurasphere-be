package com.elice.aurasphere.user.controller;

import com.elice.aurasphere.global.common.ApiRes;
import com.elice.aurasphere.user.dto.EmailCheckRequest;
import com.elice.aurasphere.user.dto.ErrorResponse;
import com.elice.aurasphere.user.dto.LoginRequest;
import com.elice.aurasphere.user.dto.NicknameCheckRequest;
import com.elice.aurasphere.user.dto.SignupRequest;
import com.elice.aurasphere.user.dto.VerificationRequest;
import com.elice.aurasphere.user.service.EmailService;
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
    private final EmailService emailService;

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
        // 이메일 인증 여부 확인
        if (!emailService.isEmailVerified(signupRequest.getEmail())) {
            return ResponseEntity.badRequest()
                .body(ApiRes.failureRes(HttpStatus.BAD_REQUEST, "이메일 인증이 완료되지 않았습니다.", null));
        }

        try {
            userService.signup(signupRequest);
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

//    @Operation(summary = "이메일 중복 확인 API", description = "회원가입 시 이메일 중복을 확인하는 API입니다.")
//    @ApiResponses(value = {
//        @ApiResponse(responseCode = "200", description = "True or False",
//            content = {@Content(schema = @Schema(implementation = ApiRes.class))})
//    })
//    @GetMapping("/user/checkEmail")
//    public ResponseEntity<ApiRes<Boolean>> checkEmailDuplication(@RequestBody EmailCheckRequest request) {
//        boolean isDuplicate = userService.checkEmailDuplication(request.getEmail());
//        return ResponseEntity.ok(ApiRes.successRes(HttpStatus.OK, isDuplicate));
//    }

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

    @Operation(summary = "이메일 인증 코드 발송",
        description = "회원가입을 위한 이메일 인증 코드를 발송합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "인증 코드 발송 성공",
            content = @Content(schema = @Schema(implementation = ApiRes.class))),
        @ApiResponse(responseCode = "400",
            description = "이미 존재하는 이메일",
            content = @Content(schema = @Schema(implementation = ApiRes.class))),
        @ApiResponse(responseCode = "500",
            description = "이메일 발송 실패",
            content = @Content(schema = @Schema(implementation = ApiRes.class)))
    })
    @PostMapping("/email/verifyCode/send")
    public ResponseEntity<ApiRes<Void>> sendVerificationEmail(@RequestBody EmailCheckRequest request) {
        if (userService.checkEmailDuplication(request.getEmail())) {
            return ResponseEntity.badRequest()
                .body(ApiRes.failureRes(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다.", null));
        }

        emailService.createAndSendVerification(request.getEmail());
        return ResponseEntity.ok(ApiRes.successRes(HttpStatus.OK, null));
    }

    @Operation(summary = "이메일 인증 코드 확인",
        description = "발송된 이메일 인증 코드의 유효성을 확인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "인증 코드 확인 성공",
            content = @Content(schema = @Schema(implementation = ApiRes.class))),
        @ApiResponse(responseCode = "400",
            description = "잘못된 인증 코드 또는 만료된 인증 코드",
            content = @Content(schema = @Schema(implementation = ApiRes.class)))
    })
    @PostMapping("/email/verify")
    public ResponseEntity<ApiRes<Void>> verifyEmail(@RequestBody VerificationRequest request) {
        boolean isVerified = emailService.verifyEmail(request.getEmail(), request.getCode());
        if (!isVerified) {
            return ResponseEntity.badRequest()
                .body(ApiRes.failureRes(HttpStatus.BAD_REQUEST, "인증에 실패했습니다.", null));
        }
        return ResponseEntity.ok(ApiRes.successRes(HttpStatus.OK, null));
    }
}


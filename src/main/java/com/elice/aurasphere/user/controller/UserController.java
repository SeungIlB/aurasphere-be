package com.elice.aurasphere.user.controller;

import com.elice.aurasphere.global.common.ApiRes;
import com.elice.aurasphere.global.exception.ErrorResponseDto;
import com.elice.aurasphere.user.dto.EmailCheckRequest;
import com.elice.aurasphere.user.dto.LoginRequest;
import com.elice.aurasphere.user.dto.NicknameCheckRequest;
import com.elice.aurasphere.user.dto.SignupRequest;
import com.elice.aurasphere.user.dto.VerificationRequest;
import com.elice.aurasphere.user.service.EmailService;
import org.springframework.validation.BindingResult;
import com.elice.aurasphere.user.service.UserService;
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
        @ApiResponse(responseCode = "S000", description = "로그인 성공",
            content = {@Content(schema = @Schema(implementation = ApiRes.class))}),
        @ApiResponse(responseCode = "U005", description = "잘못된 사용자 요청",
            content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
        @ApiResponse(responseCode = "A004", description = "잘못된 인증 정보")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiRes<Void>> login(@Valid @RequestBody LoginRequest loginRequest, BindingResult bindingResult, HttpServletResponse response) {
        log.info("Login request received for email: {}", loginRequest.getEmail());
        userService.login(loginRequest, response);
        return ResponseEntity.ok(ApiRes.successRes(HttpStatus.OK, null));
    }

    @Operation(summary = "회원가입 API", description = "새로운 사용자를 등록하는 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "S000", description = "회원가입 성공",
            content = {@Content(schema = @Schema(implementation = ApiRes.class))}),
        @ApiResponse(responseCode = "U005", description = "잘못된 사용자 요청",
            content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
        @ApiResponse(responseCode = "U002", description = "이미 존재하는 이메일",
            content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
        @ApiResponse(responseCode = "U003", description = "이미 존재하는 닉네임",
            content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiRes<Void>> signup(@Valid @RequestBody SignupRequest signupRequest) {
        userService.signup(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiRes.successRes(HttpStatus.CREATED, null));
    }

    @Operation(summary = "로그아웃 API", description = "사용자 로그아웃을 처리하는 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "S000", description = "로그아웃 성공")
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
        @ApiResponse(responseCode = "S000", description = "중복 확인 성공",
            content = @Content(schema = @Schema(implementation = ApiRes.class))),
        @ApiResponse(responseCode = "U003", description = "이미 존재하는 닉네임입니다.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/user/checkNickname")
    public ResponseEntity<ApiRes<Boolean>> checkNicknameDuplication(@RequestBody NicknameCheckRequest request) {
        boolean isDuplicate = userService.checkNicknameDuplication(request.getNickname());
        return ResponseEntity.ok(ApiRes.successRes(HttpStatus.OK, isDuplicate));
    }

    @Operation(summary = "이메일 인증 코드 발송", description = "회원가입을 위한 이메일 인증 코드를 발송합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "S000", description = "인증 코드 발송 성공",
            content = @Content(schema = @Schema(implementation = ApiRes.class))),
        @ApiResponse(responseCode = "U002", description = "이미 존재하는 이메일입니다.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "S001", description = "서버에 오류가 발생했습니다.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/email/verifyCode/send")
    public ResponseEntity<ApiRes<Void>> sendVerificationEmail(@RequestBody EmailCheckRequest request) {
        emailService.createAndSendVerification(request.getEmail());
        return ResponseEntity.ok(ApiRes.successRes(HttpStatus.OK, null));
    }

    @Operation(summary = "이메일 인증 코드 확인", description = "발송된 이메일 인증 코드의 유효성을 확인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "S000", description = "인증 코드 확인 성공",
            content = @Content(schema = @Schema(implementation = ApiRes.class))),
        @ApiResponse(responseCode = "V001", description = "유효하지 않은 인증 코드입니다.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "V002", description = "만료된 인증 코드입니다.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "V003", description = "이미 인증이 완료된 코드입니다.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/email/verify")
    public ResponseEntity<ApiRes<Void>> verifyEmail(@RequestBody VerificationRequest request) {
       emailService.verifyEmail(request.getEmail(), request.getCode());
        return ResponseEntity.ok(ApiRes.successRes(HttpStatus.OK, null));
    }
}


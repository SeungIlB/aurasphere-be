package com.elice.aurasphere.user.controller;

import com.elice.aurasphere.global.common.ApiRes;
import com.elice.aurasphere.global.exception.ErrorResponseDto;
import com.elice.aurasphere.user.dto.LoginRequestDTO;
import com.elice.aurasphere.user.dto.NicknameCheckRequestDTO;
import com.elice.aurasphere.user.dto.PasswordResetRequestDTO;
import com.elice.aurasphere.user.dto.PasswordUpdateRequestDTO;
import com.elice.aurasphere.user.dto.SignupRequestDTO;
import com.elice.aurasphere.user.entity.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api")
@Slf4j
public class UserController {
    private final UserService userService;

    @Operation(summary = "로그인 API", description = "이메일과 비밀번호로 로그인하는 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "S000", description = "로그인 성공",
            content = {@Content(schema = @Schema(implementation = ApiRes.class))}),
        @ApiResponse(responseCode = "U005", description = "잘못된 사용자 요청",
            content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))}),
        @ApiResponse(responseCode = "A004", description = "잘못된 인증 정보")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiRes<Void>> login(@Valid @RequestBody LoginRequestDTO loginRequest, HttpServletResponse response) {
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
    public ResponseEntity<ApiRes<Void>> signup(@Valid @RequestBody SignupRequestDTO signupRequest) {
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
    @GetMapping("/users/nickname")
    public ResponseEntity<ApiRes<Boolean>> checkNicknameDuplication(@RequestParam String nickname) {
        boolean isDuplicate = userService.checkNicknameDuplication(nickname);
        return ResponseEntity.ok(ApiRes.successRes(HttpStatus.OK, isDuplicate));
    }

    @Operation(summary = "비밀번호 재설정 API", description = "인증된 이메일에 대해 비밀번호를 재설정하는 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "S000", description = "비밀번호 재설정 성공",
            content = @Content(schema = @Schema(implementation = ApiRes.class))),
        @ApiResponse(responseCode = "U001", description = "유저를 찾을 수 없습니다.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "U005", description = "잘못된 사용자 요청",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/user/reset_password")
    public ResponseEntity<ApiRes<Void>> resetPassword(@Valid @RequestBody PasswordResetRequestDTO request) {
        userService.resetPassword(request.getEmail(), request.getNewPassword());
        return ResponseEntity.ok(ApiRes.successRes(HttpStatus.OK, null));
    }

    @Operation(summary = "비밀번호 수정 API", description = "현재 비밀번호와 새 비밀번호를 입력받아 비밀번호를 수정하는 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "S000", description = "비밀번호 수정 성공"),
        @ApiResponse(responseCode = "U004", description = "잘못된 비밀번호입니다.")
    })
    @PatchMapping("/user/edit/password")
    public ResponseEntity<ApiRes<Void>> updatePassword(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody PasswordUpdateRequestDTO request
    ) {
        userService.updatePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiRes.successRes(HttpStatus.OK, null));
    }
}


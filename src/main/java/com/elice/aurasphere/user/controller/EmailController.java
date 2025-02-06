package com.elice.aurasphere.user.controller;

import com.elice.aurasphere.global.common.ApiRes;
import com.elice.aurasphere.global.exception.ErrorResponseDto;
import com.elice.aurasphere.user.dto.EmailCheckRequestDTO;
import com.elice.aurasphere.user.dto.VerificationRequestDTO;
import com.elice.aurasphere.user.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Email", description = "이메일 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Slf4j
public class EmailController {

    private final EmailService emailService;

    @Operation(summary = "이메일 인증 코드 발송", description = "회원가입을 위한 이메일 인증 코드를 발송합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "S000", description = "인증 코드 발송 성공",
            content = @Content(schema = @Schema(implementation = ApiRes.class))),
        @ApiResponse(responseCode = "U002", description = "이미 존재하는 이메일입니다.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "S001", description = "서버에 오류가 발생했습니다.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/email/verification_code")
    public ResponseEntity<ApiRes<Void>> sendVerificationEmail(@RequestBody EmailCheckRequestDTO request) {
        emailService.createAndSendVerification(request.getEmail());
        return ResponseEntity.ok(ApiRes.successRes(HttpStatus.OK, null));
    }

    @Operation(summary = "비밀번호 재설정 인증 코드 발송", description = "비밀번호 재설정을 위한 이메일 인증 코드를 발송합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "S000", description = "인증 코드 발송 성공",
            content = @Content(schema = @Schema(implementation = ApiRes.class))),
        @ApiResponse(responseCode = "U001", description = "존재하지 않는 이메일입니다.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "S001", description = "서버에 오류가 발생했습니다.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/password/verification_code")
    public ResponseEntity<ApiRes<Void>> sendPasswordResetVerification(@RequestBody EmailCheckRequestDTO request) {
        emailService.createAndSendPasswordResetVerification(request.getEmail());
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
    @PostMapping("/email/verification")
    public ResponseEntity<ApiRes<Void>> verifyEmail(@RequestBody VerificationRequestDTO request) {
        emailService.verifyEmail(request.getEmail(), request.getCode());
        return ResponseEntity.ok(ApiRes.successRes(HttpStatus.OK, null));
    }
}

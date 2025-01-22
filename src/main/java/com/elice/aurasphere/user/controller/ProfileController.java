package com.elice.aurasphere.user.controller;

import com.elice.aurasphere.global.exception.ErrorResponseDto;
import com.elice.aurasphere.user.entity.CustomUserDetails;
import com.elice.aurasphere.user.dto.ProfileResponse;
import com.elice.aurasphere.user.dto.ProfileRequest;
import com.elice.aurasphere.user.service.ProfileService;
import com.elice.aurasphere.global.common.ApiRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile", description = "프로필 관련 API")
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "프로필 정보 조회", description = "현재 로그인한 사용자의 프로필 정보(닉네임, 프로필 이미지)를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "S000", description = "프로필 조회 성공",
            content = @Content(schema = @Schema(implementation = ApiRes.class))),
        @ApiResponse(responseCode = "U001", description = "유저를 찾을 수 없습니다.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping
    public ResponseEntity<ApiRes<ProfileResponse>> getProfile(
        @Parameter(hidden = true)
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ProfileResponse response = profileService.getProfile(userDetails.getId());
        return ResponseEntity.ok(ApiRes.successRes(HttpStatus.OK, response));
    }

    @Operation(summary = "프로필 수정", description = "프로필 정보(닉네임, 프로필 이미지)를 수정합니다. 닉네임과 이미지를 선택적으로 업데이트할 수 있습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "S000", description = "프로필 수정 성공",
            content = @Content(schema = @Schema(implementation = ApiRes.class))),
        @ApiResponse(responseCode = "U001", description = "유저를 찾을 수 없습니다.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "U003", description = "이미 존재하는 닉네임입니다.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "I001", description = "Key에 해당하는 이미지를 찾을 수 없습니다.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping
    public ResponseEntity<ApiRes<ProfileResponse>> updateProfile(
        @Parameter(hidden = true)
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ProfileRequest request
    ) {
        log.info("프로필 수정 요청 - 사용자 ID: {}, 이미지 키: {}, 닉네임: {}",
            userDetails.getId(),
            request.getImageKey(),
            request.getNickname());
        ProfileResponse response = profileService.updateProfile(userDetails.getId(), request);
        return ResponseEntity.ok(ApiRes.successRes(HttpStatus.OK, response));
    }
}
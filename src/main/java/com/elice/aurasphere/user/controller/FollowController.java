package com.elice.aurasphere.user.controller;

import com.elice.aurasphere.user.entity.CustomUserDetails;
import com.elice.aurasphere.global.common.ApiResponseDto;
import com.elice.aurasphere.user.dto.FollowUserResponseDTO;
import com.elice.aurasphere.user.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Follow", description = "팔로우 관련 API")
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "팔로우 API", description = "특정 사용자를 팔로우하는 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "S000", description = "팔로우 성공"),
        @ApiResponse(responseCode = "F001", description = "자기 자신을 팔로우할 수 없습니다.")
    })
    @PostMapping("/{userId}/follow")
    public ApiResponseDto<Void> followUser(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable("userId") Long userId
    ) {
        followService.follow(userDetails.getUsername(), userId);
        return ApiResponseDto.from(null);
    }

    @Operation(summary = "언팔로우 API", description = "특정 사용자를 언팔로우하는 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "S000", description = "언팔로우 성공")
    })
    @DeleteMapping("/{userId}/follow")
    public ApiResponseDto<Void> unfollowUser(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long userId
    ) {
        followService.unfollow(userDetails.getUsername(), userId);
        return ApiResponseDto.from(null);
    }

    @Operation(summary = "팔로우 상태 확인 API", description = "특정 사용자를 팔로우했는지 확인하는 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "S000", description = "팔로우 상태 확인 성공")
    })
    @GetMapping("/{userId}/follow/status")
    public ApiResponseDto<Boolean> getFollowStatus(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long userId
    ) {
        boolean isFollowing = followService.isFollowing(userDetails.getUsername(), userId);
        return ApiResponseDto.from(isFollowing);
    }

    @Operation(summary = "팔로워 목록 조회 API", description = "특정 사용자의 팔로워 목록을 조회하는 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "S000", description = "팔로워 목록 조회 성공")
    })
    @GetMapping("/me/followers")
    public ApiResponseDto<List<FollowUserResponseDTO>> getMyFollowers(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<FollowUserResponseDTO> followers = followService.getFollowers(userDetails.getId());
        return ApiResponseDto.from(followers);
    }


    @Operation(summary = "팔로잉 목록 조회 API", description = "특정 사용자의 팔로잉 목록을 조회하는 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "S000", description = "팔로잉 목록 조회 성공")
    })
    @GetMapping("/me/following")
    public ApiResponseDto<List<FollowUserResponseDTO>> getMyFollowing(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<FollowUserResponseDTO> following = followService.getFollowing(userDetails.getId());
        return ApiResponseDto.from(following);
    }

    @Operation(summary = "팔로워/팔로잉 수 조회 API", description = "특정 사용자의 팔로워와 팔로잉 수를 조회하는 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "S000", description = "팔로우 수 조회 성공")
    })
    @GetMapping("/me/follow/count")
    public ApiResponseDto<Map<String, Long>> getMyFollowCount(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Long> counts = new HashMap<>();
        counts.put("followerCount", followService.getFollowerCount(userDetails.getId()));
        counts.put("followingCount", followService.getFollowingCount(userDetails.getId()));
        return ApiResponseDto.from(counts);
    }
}
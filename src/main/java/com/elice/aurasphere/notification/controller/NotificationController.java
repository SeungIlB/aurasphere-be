package com.elice.aurasphere.notification.controller;

import com.elice.aurasphere.global.exception.ErrorResponseDto;
import com.elice.aurasphere.notification.dto.NotificationDTO;
import com.elice.aurasphere.notification.entity.Notification;
import com.elice.aurasphere.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 사용자 알림 목록 조회
     */
    @Operation(summary = "사용자 알림 목록 조회", description = "특정 사용자의 알림 목록을 최신순으로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationDTO.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/{userId}")
    public List<NotificationDTO> getUserNotifications(@PathVariable Long userId) {
        return notificationService.getUserNotifications(userId);
    }

    /**
     * 특정 알림 읽음 처리
     */
    @Operation(summary = "특정 알림 읽음 처리", description = "알림 ID를 기반으로 특정 알림을 읽음 상태로 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공",
                    content = @Content(schema = @Schema(implementation = NotificationDTO.class))),
            @ApiResponse(responseCode = "404", description = "해당 알림을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "해당 알림에 대한 접근 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping("/{notificationId}/read")
    public NotificationDTO markNotificationAsRead(@PathVariable Long notificationId) {
        return notificationService.markNotificationAsRead(notificationId);
    }
}

package com.elice.aurasphere.notification.controller;

import com.elice.aurasphere.notification.dto.NotificationDTO;
import com.elice.aurasphere.notification.entity.Notification;
import com.elice.aurasphere.notification.service.NotificationService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // 사용자 알림 목록 조회
    @GetMapping("/{userId}")
    public List<NotificationDTO> getUserNotifications(@PathVariable Long userId) {
        return notificationService.getUserNotifications(userId);
    }

    // 특정 알림 읽음 처리
    @PutMapping("/{notificationId}/read")
    public NotificationDTO markNotificationAsRead(@PathVariable Long notificationId) {
        return notificationService.markNotificationAsRead(notificationId);
    }
}


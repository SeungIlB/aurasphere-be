package com.elice.aurasphere.notification.service;

import com.elice.aurasphere.notification.dto.NotificationDTO;
import com.elice.aurasphere.notification.dto.NotificationType;
import com.elice.aurasphere.notification.entity.Notification;
import com.elice.aurasphere.notification.repository.NotificationRepository;
import com.elice.aurasphere.user.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;


    public NotificationService(NotificationRepository notificationRepository, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void sendNotification(User user, User fromUser, NotificationType type) {
        Notification notification = Notification.builder()
                .user(user)
                .fromUser(fromUser)
                .type(type)
                .isRead(false)
                .build();
        notificationRepository.save(notification);

        messagingTemplate.convertAndSend(
                "/topic/notifications/" + user.getId(), new NotificationDTO(notification)
        );
    }

    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationDTO markNotificationAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 알림이 존재하지 않습니다."));

        Notification updatedNotification = Notification.builder()
                .id(notification.getId())
                .user(notification.getUser())
                .fromUser(notification.getFromUser())
                .type(notification.getType())
                .isRead(true)
                .createdAt(notification.getCreatedAt())
                .build();

        notificationRepository.save(updatedNotification);
        return new NotificationDTO(updatedNotification);
    }
}



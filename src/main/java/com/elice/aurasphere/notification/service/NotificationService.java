package com.elice.aurasphere.notification.service;

import com.elice.aurasphere.notification.dto.NotificationDTO;
import com.elice.aurasphere.notification.dto.NotificationType;
import com.elice.aurasphere.notification.entity.Notification;
import com.elice.aurasphere.notification.repository.NotificationRepository;
import com.elice.aurasphere.user.entity.User;
import com.elice.aurasphere.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {


    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;  // WebSocket 메시지 전송을 위한 템플릿
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, SimpMessagingTemplate messagingTemplate,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createNotification(User fromUser, User toUser, NotificationType type) {
        // 알림 메시지 생성
        Notification notification = Notification.builder()
                .user(toUser)
                .fromUser(fromUser)
                .type(type)
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        // NotificationDTO 생성
        NotificationDTO notificationDTO = new NotificationDTO(notification);  // 주어진 생성자 사용

        // 실시간 알림 전송
        messagingTemplate.convertAndSendToUser(toUser.getId().toString(), "/queue/notifications", notificationDTO);
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



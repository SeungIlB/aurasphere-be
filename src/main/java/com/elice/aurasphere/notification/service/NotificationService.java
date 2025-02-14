package com.elice.aurasphere.notification.service;

import com.elice.aurasphere.global.exception.CustomException;
import com.elice.aurasphere.global.exception.ErrorCode;
import com.elice.aurasphere.notification.dto.NotificationDTO;
import com.elice.aurasphere.notification.dto.NotificationType;
import com.elice.aurasphere.notification.entity.Notification;
import com.elice.aurasphere.notification.repository.NotificationRepository;
import com.elice.aurasphere.user.entity.CustomUserDetails;
import com.elice.aurasphere.user.entity.User;
import com.elice.aurasphere.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, SimpMessagingTemplate messagingTemplate,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createNotification(User fromUser, User toUser, NotificationType type) {
        if (fromUser == null || toUser == null) {
            throw new CustomException(ErrorCode.NOTIFICATION_TYPE_NULL);
        }

        // NotificationType이 null인 경우 예외 처리
        if (type == null) {
            throw new CustomException(ErrorCode.NOTIFICATION_TYPE_NULL);
        }

        // fromUser와 toUser가 같으면 예외 발생
        if (fromUser.getId().equals(toUser.getId())) {
            throw new CustomException(ErrorCode.NOTIFICATION_SAME_USER);
        }

        Notification notification = Notification.builder()
                .user(toUser)
                .fromUser(fromUser)
                .type(type)
                .isRead(false)
                .createdAt(LocalDateTime.now()) // 명시적으로 추가
                .build();

        notificationRepository.save(notification);
        NotificationDTO notificationDTO = new NotificationDTO(notification);

        // WebSocket을 통해 실시간 알림 전송
        messagingTemplate.convertAndSend("/user/" + toUser.getId() + "/queue/notifications", notificationDTO);
    }

    public List<NotificationDTO> getUserNotifications(CustomUserDetails userDetails) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userDetails.getId())
                .stream()
                .map(NotificationDTO::new)
                .collect(Collectors.toList());
    }


    @Transactional
    public void markAllNotificationsAsRead(CustomUserDetails customUserDetails) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(customUserDetails.getId());

        notifications.forEach(Notification::markAsRead); // 각 알림을 읽음 처리
    }

}


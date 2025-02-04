package com.elice.aurasphere;

import com.elice.aurasphere.global.exception.CustomException;
import com.elice.aurasphere.global.exception.ErrorCode;
import com.elice.aurasphere.notification.dto.NotificationDTO;
import com.elice.aurasphere.notification.dto.NotificationType;
import com.elice.aurasphere.notification.entity.Notification;
import com.elice.aurasphere.notification.repository.NotificationRepository;
import com.elice.aurasphere.notification.service.NotificationService;
import com.elice.aurasphere.user.entity.CustomUserDetails;
import com.elice.aurasphere.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private User fromUser;

    @Mock
    private User toUser;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateNotification_withValidData_shouldCreateNotification() {
        // Given
        NotificationType type = NotificationType.FOLLOW;
        when(fromUser.getId()).thenReturn(1L);
        when(toUser.getId()).thenReturn(2L);

        // When
        notificationService.createNotification(fromUser, toUser, type);

        // Then
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(NotificationDTO.class));
    }

    @Test
    void testCreateNotification_withNullFromUser_shouldThrowException() {
        // Given
        NotificationType type = NotificationType.FOLLOW;

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            notificationService.createNotification(null, toUser, type);
        });

        assertEquals(ErrorCode.NOTIFICATION_TYPE_NULL, exception.getErrorCode());
    }

    @Test
    void testCreateNotification_withNullType_shouldThrowException() {
        // Given
        when(fromUser.getId()).thenReturn(1L);
        when(toUser.getId()).thenReturn(2L);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            notificationService.createNotification(fromUser, toUser, null);
        });

        assertEquals(ErrorCode.NOTIFICATION_TYPE_NULL, exception.getErrorCode());
    }

    @Test
    void testGetUserNotifications_shouldReturnNotificationList() {
        // Given
        Long userId = 1L;
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(userId);

        Notification notification = mock(Notification.class);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(notification));

        // When
        List<NotificationDTO> result = notificationService.getUserNotifications(userDetails);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }


    @Test
    void testMarkAllNotificationsAsRead_shouldMarkAllAsRead() {
        // Given
        Long userId = 1L;
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(userId);

        List<Notification> notifications = List.of(mock(Notification.class), mock(Notification.class));
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)).thenReturn(notifications);

        // When
        notificationService.markAllNotificationsAsRead(userDetails);

        // Then
        verify(notifications.get(0), times(1)).markAsRead();
        verify(notifications.get(1), times(1)).markAsRead();
    }

    @Test
    void testMarkAllNotificationsAsRead_withNoUnreadNotifications_shouldNotThrowException() {
        // Given
        Long userId = 1L;
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(userId);

        List<Notification> notifications = List.of(); // 읽지 않은 알림이 없음
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)).thenReturn(notifications);

        // When & Then (예외가 발생하지 않아야 함)
        assertDoesNotThrow(() -> notificationService.markAllNotificationsAsRead(userDetails));
    }
}

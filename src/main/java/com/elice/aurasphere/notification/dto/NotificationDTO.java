package com.elice.aurasphere.notification.dto;

import com.elice.aurasphere.notification.entity.Notification;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationDTO {
    private Long id;
    private String message;
    private boolean isRead;

    public NotificationDTO(Notification notification) {
        this.id = notification.getId();
        this.message = notification.generateMessage();
        this.isRead = notification.isRead();
    }
}


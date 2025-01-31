package com.elice.aurasphere.notification.entity;

import com.elice.aurasphere.notification.dto.NotificationType;
import com.elice.aurasphere.user.entity.Profile;
import com.elice.aurasphere.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 알림을 받는 사용자


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser; // 알림을 발생시킨 사용자

    @Enumerated(EnumType.STRING)
    private NotificationType type; // 알림 유형 (댓글, 팔로우, 좋아요)

    private boolean isRead; // 읽음 여부

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public String generateMessage() {
        String fromUserName = fromUser.getProfile().getNickname();
        switch (type) {
            case COMMENT:
                return fromUserName + "님이 게시물에 댓글을 달았습니다.";
            case FOLLOW:
                return fromUserName + "님이 팔로우를 하였습니다.";
            case LIKE:
                return fromUserName + "님이 게시물에 좋아요를 달았습니다.";
            default:
                return "새로운 알림이 도착했습니다.";
        }
    }
}



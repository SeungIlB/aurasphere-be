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
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "is_read")
    private boolean is_read;
    
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public String generateMessage() {
        String fromUserName = (fromUser != null && fromUser.getProfile() != null) ? fromUser.getProfile().getNickname() : "알 수 없는 사용자";
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

    public void markAsRead() {
        this.is_read = true;
    }
}



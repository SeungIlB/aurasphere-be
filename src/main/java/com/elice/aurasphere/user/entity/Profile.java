package com.elice.aurasphere.user.entity;

import com.elice.aurasphere.global.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profile")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "profile_url", nullable = false)
    private String profileUrl;

    @Column(name = "profile_url_expiry_date")
    private LocalDateTime profileUrlExpiryDate;

    public void initUser(User user) {
        this.user = user;
    }

    // 프로필 업데이트를 위한 메서드
    public void updateProfileNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
        this.profileUrlExpiryDate = null;
    }

    public void updateProfileUrlWithExpiry(String profileUrl, LocalDateTime expiryDate) {
        this.profileUrl = profileUrl;
        this.profileUrlExpiryDate = expiryDate;
    }
}
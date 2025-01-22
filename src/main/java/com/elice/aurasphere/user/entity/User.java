package com.elice.aurasphere.user.entity;

import com.elice.aurasphere.global.audit.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 25, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Profile profile;

    // 팔로워 목록 (나를 팔로우하는 사람들)
    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Follow> followers = new HashSet<>();

    // 팔로잉 목록 (내가 팔로우하는 사람들)
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Follow> following = new HashSet<>();

    public void addProfile(Profile profile) {
        this.profile = profile;
        profile.initUser(this);  // Profile 쪽에도 연관관계 설정
    }

    // 팔로워 추가
    public void addFollower(Follow follow) {
        this.followers.add(follow);
    }

    // 팔로워 제거
    public void removeFollower(Follow follow) {
        this.followers.remove(follow);
    }

    // 팔로잉 추가
    public void addFollowing(Follow follow) {
        this.following.add(follow);
    }

    // 팔로잉 제거
    public void removeFollowing(Follow follow) {
        this.following.remove(follow);
    }
}

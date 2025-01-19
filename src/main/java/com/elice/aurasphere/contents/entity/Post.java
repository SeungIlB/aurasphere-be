package com.elice.aurasphere.contents.entity;

import com.elice.aurasphere.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//setter 어노테이션 허용 시 어디서는 객체의 변경이 가능하기 때문에 사용 지양하기
//대신 빌더 패턴 사용
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post")
@Entity
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;

    @Column(length = 150)
    private String content;

    @Column
    private Long likeCnt;

    @Column
    private LocalDateTime deletedDate;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Comment> Comments = new ArrayList<>();

    @Builder
    public Post(
            String content,
            Long likeCnt
    ) {
        this.content = content;
        this.likeCnt = likeCnt;
    }

    public void updatePost(String content) { this.content = content; }

}


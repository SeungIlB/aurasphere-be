package com.elice.aurasphere.contents.entity;

import com.elice.aurasphere.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "comment")
@Entity
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus commentStatus;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "p_comment_id")
    private Comment pComment;

    @Column(length = 100, nullable = false)
    private String content;

    @Column
    private LocalDateTime deletedDate;

    public enum CommentStatus {
        COMMENT,
        REPLY
    }
}

package com.elice.aurasphere.contents.entity;

import com.elice.aurasphere.global.audit.BaseEntity;
import com.elice.aurasphere.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
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


    @Builder
    public Comment(
            User user,
            CommentStatus commentStatus,
            Post post,
            Comment pComment,
            String content
    ){
        this.user = user;
        this.commentStatus = commentStatus;
        this.post = post;
        this.pComment = pComment;
        this.content = content;
    }

    public enum CommentStatus {
        COMMENT,
        REPLY
    }

    public void updateComment(String content) { this.content = content; }

    public void removeComment() { this.deletedDate = LocalDateTime.now(); }
}

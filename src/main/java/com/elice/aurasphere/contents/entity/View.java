package com.elice.aurasphere.contents.entity;


import com.elice.aurasphere.global.audit.BaseEntity;
import com.elice.aurasphere.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "view")
@Entity
public class View extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private Long viewCnt;

    @Builder
    public View(Post post, Long viewCnt){
        this.post = post;
        this.viewCnt = viewCnt;
    }

    public void countUp(){
        this.viewCnt++;
    }

}

package com.elice.aurasphere.contents.entity;

import com.elice.aurasphere.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "image")
@Entity
public class Image extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Lob
    @JoinColumn(name = "img_url", nullable = false)
    private String imgUrl;


    @Builder
    public Image(Long id, Post post, String imgUrl){
        this.id = id;
        this.post = post;
        this.imgUrl = imgUrl;
    }

}
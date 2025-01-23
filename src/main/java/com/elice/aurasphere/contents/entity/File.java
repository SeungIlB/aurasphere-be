package com.elice.aurasphere.contents.entity;

import com.elice.aurasphere.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "file")
@Entity
public class File extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Lob
    @JoinColumn(name = "url", nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType;

    public enum FileType {
        IMAGE,
        VIDEO
    }



    @Builder
    public File(Long id, Post post, String url, FileType fileType){
        this.id = id;
        this.post = post;
        this.url = url;
        this.fileType = fileType;
    }

}
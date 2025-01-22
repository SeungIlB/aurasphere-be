package com.elice.aurasphere.contents.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.elice.aurasphere.contents.entity.QImage.image;


@Repository
public class ImageCustomRepositoryImpl implements ImageCustomRepository{

    private final JPAQueryFactory queryFactory;

    public ImageCustomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<String> findImagesByPostId(Long postId) {

        return queryFactory.select(image.imgUrl)
                .from(image)
                .where(image.post.id.eq(postId))
                .fetch();
    }
}
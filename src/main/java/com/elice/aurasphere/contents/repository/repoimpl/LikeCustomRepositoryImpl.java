package com.elice.aurasphere.contents.repository.repoimpl;

import com.elice.aurasphere.contents.repository.LikeCustomRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import static com.elice.aurasphere.contents.entity.QLike.like;
import static com.elice.aurasphere.contents.entity.QPost.post;

@Repository
public class LikeCustomRepositoryImpl implements LikeCustomRepository {

    private final JPAQueryFactory queryFactory;

    public LikeCustomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Long findlikeCntByPostId(Long postId) {

        Long count = queryFactory
                .select(like.count())
                .from(like)
                .where(like.post.id.eq(postId))
                .fetchOne();

        return count != null ? count : 0L;
    }
}

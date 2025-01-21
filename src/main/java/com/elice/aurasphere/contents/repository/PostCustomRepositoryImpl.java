package com.elice.aurasphere.contents.repository;

import com.elice.aurasphere.contents.entity.Post;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.elice.aurasphere.contents.entity.QImage.image;
import static com.elice.aurasphere.contents.entity.QPost.post;


@Repository
public class PostCustomRepositoryImpl implements PostCustomRepository{

    private final JPAQueryFactory queryFactory;

    public PostCustomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }


    @Override
    public List<Post> findMyPosts(Long userId, Pageable pageable, Long cursor) {

        List<Post> postList = queryFactory
                .selectFrom(post)
                .where(post.user.id.eq(userId), checkCondition(cursor))
                .orderBy(post.id.desc())
                .limit(pageable.getPageSize())
                .fetch();

        return postList;
    }

    private BooleanExpression checkCondition(Long cursor){
        return cursor == null ? null : post.id.lt(cursor);
    }
}

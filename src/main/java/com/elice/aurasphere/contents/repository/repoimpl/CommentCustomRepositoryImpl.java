package com.elice.aurasphere.contents.repository.repoimpl;

import com.elice.aurasphere.contents.entity.Comment;
import com.elice.aurasphere.contents.entity.Post;
import com.elice.aurasphere.contents.repository.CommentCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.elice.aurasphere.contents.entity.QComment.comment;
import static com.elice.aurasphere.contents.entity.QPost.post;

@Repository
public class CommentCustomRepositoryImpl implements CommentCustomRepository {

    private final JPAQueryFactory queryFactory;

    public CommentCustomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<Comment> findCommentsByPostId(Long postId, int size, Long cursor) {

        return queryFactory
                .selectFrom(comment)
                .where(comment.post.id.eq(postId), checkCondition(cursor))
                .orderBy(comment.id.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression checkCondition(Long cursor){

        BooleanExpression condition = post.deletedDate.isNull();

        if (cursor != 0) {
            condition = condition.and(post.id.lt(cursor));
        }

        return condition;
    }

}

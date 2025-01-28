package com.elice.aurasphere.contents.repository;

import com.elice.aurasphere.contents.dto.FilterResDTO;
import com.elice.aurasphere.contents.entity.Post;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elice.aurasphere.contents.entity.QLike.like;
import static com.elice.aurasphere.contents.entity.QPost.post;


@Repository
public class PostCustomRepositoryImpl implements PostCustomRepository{

    private final JPAQueryFactory queryFactory;

    public PostCustomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public FilterResDTO findAllPostsByAsc(Long userId, int size, Long postCursor) {

        List<Post> results = queryFactory
                .selectFrom(post)
                .where(checkCondition(postCursor))
                .orderBy(post.id.desc())
                .limit(size)
                .fetch();

        return FilterResDTO.builder()
                .postList(results)
                .build();
    }

    @Override
    public List<Post> findMyPosts(Long userId, int size, Long postCursor) {

        return queryFactory
                .selectFrom(post)
                .where(post.user.id.eq(userId), checkCondition(postCursor))
                .orderBy(post.id.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public FilterResDTO findPostsByLikes(Long userId, int size, Long postCursor, Optional<Long> filterCursor) {

        List<Tuple> results = queryFactory
                .select(post, like.count().as("likeCnt"))
                .from(post)
                .leftJoin(like).on(like.post.id.eq(post.id))
                .groupBy(post.id)
                .having(checkLikeCondition(postCursor, filterCursor))
                .orderBy(like.count().desc(), post.id.desc())
                .limit(size)
                .fetch();

        List<Post> postList = results.stream()
                .map(tuple -> tuple.get(post))
                .toList();

        // 마지막 게시글의 좋아요 수
        Long lastLike = results.isEmpty() ? 0L : results.get(results.size() - 1).get(1, Long.class);


        return FilterResDTO.builder()
                .postList(postList)
                .filterCursor(lastLike)
                .build();
    }

    private BooleanExpression checkCondition(Long cursor){
        return cursor == 0 ? null : post.id.lt(cursor);
    }
    private BooleanExpression checkLikeCondition(Long postCursor, Optional<Long> filterCursor){
        return filterCursor.isPresent() ? like.count().lt(filterCursor.get())
                .or(like.count().eq(filterCursor.get()).and(post.id.lt(postCursor))) : null;
    }
}

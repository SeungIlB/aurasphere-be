package com.elice.aurasphere.contents.repository.repoimpl;

import com.elice.aurasphere.contents.repository.ViewCustomRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import static com.elice.aurasphere.contents.entity.QView.view;


@Repository
public class ViewCustomRepositoryImpl implements ViewCustomRepository {

    private final JPAQueryFactory queryFactory;

    public ViewCustomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Long findViewCntByPostId(Long postId) {
        return queryFactory
                .select(view.viewCnt)
                .from(view)
                .where(view.post.id.eq(postId))
                .fetchOne();
    }
}

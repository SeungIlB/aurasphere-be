package com.elice.aurasphere.contents.repository;

import com.elice.aurasphere.contents.dto.FileDTO;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.elice.aurasphere.contents.entity.QFile.file;


@Repository
public class FileCustomRepositoryImpl implements FileCustomRepository{

    private final JPAQueryFactory queryFactory;

    public FileCustomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<FileDTO> findFilesByPostId(Long postId) {

        return queryFactory.select(Projections.constructor(FileDTO.class,
                        file.fileType
                        ,file.url))
                .from(file)
                .where(file.post.id.eq(postId))
                .fetch();
    }
}
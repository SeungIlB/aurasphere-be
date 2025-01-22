package com.elice.aurasphere.contents.repository;

import java.util.List;

public interface ImageCustomRepository {

    List<String> findImagesByPostId(Long postId);
}

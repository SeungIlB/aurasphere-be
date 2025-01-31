package com.elice.aurasphere.contents.repository;

import com.elice.aurasphere.contents.dto.FileDTO;

import java.util.List;

public interface FileCustomRepository {

    List<FileDTO> findFilesByPostId(Long postId);
}

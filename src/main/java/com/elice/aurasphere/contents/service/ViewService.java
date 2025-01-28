package com.elice.aurasphere.contents.service;

import com.elice.aurasphere.contents.repository.ViewRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ViewService {

    private final ViewRepository viewRepository;

    public ViewService(ViewRepository viewRepository) {
        this.viewRepository = viewRepository;
    }


    public Long getViewCnt(Long postId){
        return viewRepository.findViewCntByPostId(postId);
    }
}

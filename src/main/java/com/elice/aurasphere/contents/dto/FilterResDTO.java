package com.elice.aurasphere.contents.dto;


import com.elice.aurasphere.contents.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class FilterResDTO {

    private List<Post> postList;
    private Long filterCursor;

    @Builder
    public FilterResDTO(List<Post> postList, Long filterCursor) {
        this.postList = postList;
        this.filterCursor = filterCursor;
    }
}

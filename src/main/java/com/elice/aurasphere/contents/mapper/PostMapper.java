package com.elice.aurasphere.contents.mapper;


import com.elice.aurasphere.contents.dto.PostResponseDTO;
import com.elice.aurasphere.contents.dto.PostUpdateDTO;
import com.elice.aurasphere.contents.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {

    Post PostUpdateDtoToPost(PostUpdateDTO postUpdateDTO);

    PostResponseDTO postToPostResDto(Post post);
}

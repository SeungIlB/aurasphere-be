package com.elice.aurasphere.post;


import com.elice.aurasphere.contents.controller.PostController;
import com.elice.aurasphere.contents.dto.PostListResDTO;
import com.elice.aurasphere.contents.dto.PostResDTO;
import com.elice.aurasphere.contents.service.PostService;
import com.elice.aurasphere.global.authentication.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testReadPostByMyId() throws Exception {
        // given
        PostListResDTO mockPostList = PostListResDTO.builder()
                .postList(Arrays.asList(
                        PostResDTO.builder()
                                .id(1L)
                                .content("첫 번째 게시글")
                                .likeCnt(0L)
                                .viewCnt(0L)
                                .isLiked(false)
                                .commentCnt(0L)
                                .urls(null)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build(),
                        PostResDTO.builder()
                                .id(2L)
                                .content("두 번째 게시글")
                                .likeCnt(0L)
                                .viewCnt(0L)
                                .isLiked(false)
                                .commentCnt(0L)
                                .urls(null)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build()
                ))
                .post_cursor(2L) // 마지막 게시글 번호
                .filter_cursor(1L) // 예시 필터링 커서 값
                .hasNext(false) // 다음 데이터 여부
                .build();

        when(postService.getMyPosts(any(String.class), any(Integer.class), any(Long.class)))
                .thenReturn(mockPostList);

        String token = jwtTokenProvider.createAccessToken("test@test.com", Collections.singletonList("USER"));

        // when
        mockMvc.perform(get("/posts/me")
                        .param("size", "5")
                        .param("cursor", "0")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // then: HTTP 상태 코드 확인
                .andExpect(jsonPath("$.data.postList").isArray()) // 응답 데이터가 배열인지 확인
                .andExpect(jsonPath("$.data.postList.length()").value(2)) // 게시글 수 확인
                .andExpect(jsonPath("$.data.postList[0].title").value("첫 번째 게시글")) // 첫 번째 게시글 제목 확인
                .andExpect(jsonPath("$.data.postList[1].title").value("두 번째 게시글")); // 두 번째 게시글 제목 확인
    }
}

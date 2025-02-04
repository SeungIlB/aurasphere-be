package com.elice.aurasphere.post;


import com.elice.aurasphere.contents.entity.Post;
import com.elice.aurasphere.contents.repository.PostRepository;
import com.elice.aurasphere.user.entity.User;
import com.elice.aurasphere.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
public class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;


    /*
    게시글 테스트를 위한 유저 생성
     */
    @BeforeEach
    public void userSetup() {
        user = User.builder()
                .id(1L)
                .email("test1@test.com")
                .password(passwordEncoder.encode("Test1234!"))
                .role("USER") // 기본 역할 설정
                .build();
    }


    /*
    게시글 작성 테스트
     */
    @Test
    public void testPostIsSavedSucceccfully(){

        String content = "테스트 코드로 작성한 글입니다.";

        //given
        Post newPost = Post.builder()
                .user(user)
                .content(content)
                .build();

        //when
        Post savedPost = postRepository.save(newPost);

        //then
        assertThat(savedPost).isNotNull();
        assertThat(savedPost.getId()).isNotNull();
        assertThat(savedPost.getContent()).isEqualTo(content);

        assertThat(postRepository.findById(savedPost.getId())).isPresent();

    }
}

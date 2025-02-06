package com.elice.aurasphere;

import com.elice.aurasphere.user.entity.Profile;
import com.elice.aurasphere.user.entity.User;
import com.elice.aurasphere.global.common.ApiRes;
import com.elice.aurasphere.user.dto.FollowUserResponseDTO;
import com.elice.aurasphere.user.repository.FollowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FollowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private FollowRepository followRepository;

    private User targetUser;
    private Profile targetProfile;

    @BeforeEach
    void setUpFollowTest() {
        createTestUser(); // 팔로워 유저 생성

        // 팔로우 대상 유저 생성
        targetUser = User.builder()
            .email("target@example.com")
            .password(passwordEncoder.encode("Password123!"))
            .role("USER")
            .build();

        targetProfile = Profile.builder()
            .user(targetUser)
            .nickname("targetUser")
            .profileUrl("DEFAULT")
            .build();

        userRepository.save(targetUser);
        profileRepository.save(targetProfile);

        performLogin(); // 팔로워 유저로 로그인
    }

    @Test
    @Order(1)
    @DisplayName("팔로우 테스트")
    void followUserTest() {
        // Given
        HttpEntity<?> entity = new HttpEntity<>(createAuthorizationHeader());

        // When
        ResponseEntity<ApiRes> response = restTemplate.exchange(
            createURLWithPort("/api/user/" + targetUser.getId() + "/follow"),
            HttpMethod.POST,
            entity,
            ApiRes.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 실제 팔로우 관계가 생성되었는지 확인
        assertTrue(followRepository.existsByFollowerAndFollowing(testUser, targetUser));

        // 팔로워/팔로잉 수 확인
        ResponseEntity<ApiRes<Map<String, Long>>> countResponse = restTemplate.exchange(
            createURLWithPort("/api/user/me/follow/count"),
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<ApiRes<Map<String, Long>>>() {}
        );

        Map<String, Long> counts = countResponse.getBody().getData();
        assertEquals(1L, counts.get("followingCount"));
    }

    @Test
    @Order(2)
    @DisplayName("팔로우 상태 확인 테스트")
    void checkFollowStatusTest() {
        // Given
        HttpEntity<?> entity = new HttpEntity<>(createAuthorizationHeader());

        // 먼저 팔로우
        restTemplate.exchange(
            createURLWithPort("/api/user/" + targetUser.getId() + "/follow"),
            HttpMethod.POST,
            entity,
            ApiRes.class
        );

        // When
        ResponseEntity<ApiRes<Boolean>> response = restTemplate.exchange(
            createURLWithPort("/api/user/" + targetUser.getId() + "/follow/status"),
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<ApiRes<Boolean>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData());
    }

    @Test
    @Order(3)
    @DisplayName("팔로워 목록 조회 테스트")
    void getFollowersTest() {
        // Given
        HttpEntity<?> entity = new HttpEntity<>(createAuthorizationHeader());

        // 먼저 팔로우
        restTemplate.exchange(
            createURLWithPort("/api/user/" + targetUser.getId() + "/follow"),
            HttpMethod.POST,
            entity,
            ApiRes.class
        );

        // When
        ResponseEntity<ApiRes<List<FollowUserResponseDTO>>> response = restTemplate.exchange(
            createURLWithPort("/api/user/me/followers"),
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<ApiRes<List<FollowUserResponseDTO>>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getData());
    }

    @Test
    @Order(4)
    @DisplayName("팔로잉 목록 조회 테스트")
    void getFollowingTest() {
        // Given
        HttpEntity<?> entity = new HttpEntity<>(createAuthorizationHeader());

        // 먼저 팔로우
        restTemplate.exchange(
            createURLWithPort("/api/user/" + targetUser.getId() + "/follow"),
            HttpMethod.POST,
            entity,
            ApiRes.class
        );

        // When
        ResponseEntity<ApiRes<List<FollowUserResponseDTO>>> response = restTemplate.exchange(
            createURLWithPort("/api/user/me/following"),
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<ApiRes<List<FollowUserResponseDTO>>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<FollowUserResponseDTO> following = response.getBody().getData();
        assertNotNull(following);
        assertFalse(following.isEmpty());
        assertEquals(targetUser.getEmail(), following.get(0).getEmail());
    }

    @Test
    @Order(5)
    @DisplayName("언팔로우 테스트")
    void unfollowUserTest() {
        // Given
        HttpEntity<?> entity = new HttpEntity<>(createAuthorizationHeader());

        // 먼저 팔로우
        restTemplate.exchange(
            createURLWithPort("/api/user/" + targetUser.getId() + "/follow"),
            HttpMethod.POST,
            entity,
            ApiRes.class
        );

        // When
        ResponseEntity<ApiRes> response = restTemplate.exchange(
            createURLWithPort("/api/user/" + targetUser.getId() + "/follow"),
            HttpMethod.DELETE,
            entity,
            ApiRes.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 팔로우 관계가 제거되었는지 확인
        assertFalse(followRepository.existsByFollowerAndFollowing(testUser, targetUser));

        // 팔로워/팔로잉 수 확인
        ResponseEntity<ApiRes<Map<String, Long>>> countResponse = restTemplate.exchange(
            createURLWithPort("/api/user/me/follow/count"),
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<ApiRes<Map<String, Long>>>() {}
        );

        Map<String, Long> counts = countResponse.getBody().getData();
        assertEquals(0L, counts.get("followingCount"));
    }
}
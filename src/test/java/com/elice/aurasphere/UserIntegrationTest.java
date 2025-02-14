package com.elice.aurasphere;

import com.elice.aurasphere.user.dto.LoginRequestDTO;
import com.elice.aurasphere.user.dto.PasswordUpdateRequestDTO;
import com.elice.aurasphere.user.dto.SignupRequestDTO;
import com.elice.aurasphere.user.entity.User;
import com.elice.aurasphere.user.entity.Profile;
import com.elice.aurasphere.global.common.ApiRes;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserIntegrationTest extends BaseIntegrationTest {

    @Test
    @Order(1)
    @DisplayName("회원가입 통합 테스트")
    void signupIntegrationTest() {
        // Given
        SignupRequestDTO signupRequest = SignupRequestDTO.builder()
            .email("test@example.com")
            .password("Password123!")
            .nickname("testUser")
            .build();

        // When
        ResponseEntity<ApiRes> response = restTemplate.postForEntity(
            createURLWithPort("/api/signup"),
            signupRequest,
            ApiRes.class
        );

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        // DB에서 생성된 유저 확인
        Optional<User> createdUser = userRepository.findByEmail("test@example.com");
        assertTrue(createdUser.isPresent());
        assertEquals("test@example.com", createdUser.get().getEmail());

        // 프로필 생성 확인
        Optional<Profile> createdProfile = profileRepository.findByUserId(createdUser.get().getId());
        assertTrue(createdProfile.isPresent());
        assertEquals("testUser", createdProfile.get().getNickname());
    }

    @Test
    @Order(2)
    @DisplayName("로그인 통합 테스트")
    void loginIntegrationTest() {
        // Given
        createTestUser();
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
            .email("test@example.com")
            .password("Password123!")
            .build();

        // When
        ResponseEntity<ApiRes> response = restTemplate.postForEntity(
            createURLWithPort("/api/login"),
            loginRequest,
            ApiRes.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getData());
    }

    @Test
    @Order(3)
    @DisplayName("로그인 실패 테스트 - 잘못된 비밀번호")
    void loginFailureTest() {
        // Given
        createTestUser();
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
            .email("test@example.com")
            .password("WrongPassword123!")
            .build();

        // When
        HttpEntity<LoginRequestDTO> request = new HttpEntity<>(loginRequest);
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort("/api/login"),
            HttpMethod.POST,
            request,
            String.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Order(4)
    @DisplayName("닉네임 중복 확인 테스트")
    void checkNicknameDuplicationTest() {
        // Given
        createTestUser();

        // When
        ResponseEntity<ApiRes> response = restTemplate.getForEntity(
            createURLWithPort("/api/users/nickname?nickname=testUser"),
            ApiRes.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().getData());
    }

    @Test
    @Order(5)
    @DisplayName("비밀번호 수정 테스트")
    void updatePasswordTest() {
        // Given
        createTestUser();
        performLogin();

        PasswordUpdateRequestDTO updateRequest = PasswordUpdateRequestDTO.builder()
            .currentPassword("Password123!")
            .newPassword("NewPassword123!")
            .newPasswordConfirm("NewPassword123!")
            .build();

        HttpEntity<PasswordUpdateRequestDTO> entity = new HttpEntity<>(updateRequest, createAuthorizationHeader());

        // When
        ResponseEntity<ApiRes> response = restTemplate.exchange(
            createURLWithPort("/api/user/edit/password"),
            HttpMethod.PATCH,
            entity,
            ApiRes.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 새 비밀번호로 로그인 시도
        LoginRequestDTO newLoginRequest = LoginRequestDTO.builder()
            .email("test@example.com")
            .password("NewPassword123!")
            .build();

        ResponseEntity<ApiRes> loginResponse = restTemplate.postForEntity(
            createURLWithPort("/api/login"),
            newLoginRequest,
            ApiRes.class
        );

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
    }

    @Test
    @Order(6)
    @DisplayName("로그아웃 통합 테스트")
    void logoutIntegrationTest() {
        // Given
        createTestUser();
        performLogin();
        HttpEntity<?> entity = new HttpEntity<>(createAuthorizationHeader());

        // When
        ResponseEntity<ApiRes> response = restTemplate.exchange(
            createURLWithPort("/api/logout"),
            HttpMethod.POST,
            entity,
            ApiRes.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
package com.elice.aurasphere;

import com.elice.aurasphere.user.dto.EmailCheckRequestDTO;
import com.elice.aurasphere.user.dto.VerificationRequestDTO;
import com.elice.aurasphere.user.entity.EmailVerification;
import com.elice.aurasphere.user.repository.EmailVerificationRepository;
import com.elice.aurasphere.global.common.ApiRes;
import com.elice.aurasphere.global.exception.ErrorCode;
import com.elice.aurasphere.global.exception.ErrorResponseDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmailIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @MockBean
    private JavaMailSender emailSender;

    private static final String TEST_EMAIL = "test@example.com";
    private static String verificationCode;

    @BeforeEach
    void setUp() {
        super.setUp();
        emailVerificationRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("이메일 인증 코드 발송 테스트 - 신규 이메일")
    void sendVerificationEmailTest() {
        // Given
        EmailCheckRequestDTO request = EmailCheckRequestDTO.builder()
            .email(TEST_EMAIL)
            .build();

        // When
        ResponseEntity<ApiRes> response = restTemplate.postForEntity(
            createURLWithPort("/api/users/email/verification_code"),
            request,
            ApiRes.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(emailSender, times(1)).send(any(SimpleMailMessage.class));

        // 인증 코드가 DB에 저장되었는지 확인
        Optional<EmailVerification> verification = emailVerificationRepository.findByEmail(TEST_EMAIL);
        assertTrue(verification.isPresent());
        assertFalse(verification.get().isVerified());
        assertTrue(verification.get().getExpiryDate().isAfter(LocalDateTime.now()));

        // 다음 테스트를 위해 인증 코드 저장
        verificationCode = verification.get().getVerificationCode();
    }

    @Test
    @Order(2)
    @DisplayName("이메일 인증 코드 발송 테스트 - 이미 존재하는 이메일")
    void sendVerificationEmailToExistingUserTest() {
        // Given
        createTestUser(); // 이미 가입된 사용자 생성
        EmailCheckRequestDTO request = EmailCheckRequestDTO.builder()
            .email(TEST_EMAIL)
            .build();

        try {
            // When
            restTemplate.postForEntity(
                createURLWithPort("/api/users/email/verification_code"),
                request,
                ApiRes.class
            );
        } catch (RestClientResponseException e) {
            // Then
            assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
            assertTrue(e.getResponseBodyAsString().contains(ErrorCode.EMAIL_ALREADY_EXISTS.getMessage()));
        }
    }

    @Test
    @Order(3)
    @DisplayName("이메일 인증 성공 테스트")
    void verifyEmailSuccessTest() {
        // Given
        // 먼저 인증 코드 발송
        EmailCheckRequestDTO emailRequest = EmailCheckRequestDTO.builder()
            .email(TEST_EMAIL)
            .build();
        restTemplate.postForEntity(
            createURLWithPort("/api/users/email/verification_code"),
            emailRequest,
            ApiRes.class
        );

        // 저장된 인증 코드로 인증 요청
        Optional<EmailVerification> verification = emailVerificationRepository.findByEmail(TEST_EMAIL);
        assertTrue(verification.isPresent());

        VerificationRequestDTO verificationRequest = VerificationRequestDTO.builder()
            .email(TEST_EMAIL)
            .code(verification.get().getVerificationCode())
            .build();

        // When
        ResponseEntity<ApiRes> response = restTemplate.postForEntity(
            createURLWithPort("/api/users/email/verification"),
            verificationRequest,
            ApiRes.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // DB에서 인증 상태 확인
        EmailVerification verifiedEmail = emailVerificationRepository.findByEmail(TEST_EMAIL)
            .orElseThrow();
        assertTrue(verifiedEmail.isVerified());
    }

    @Test
    @Order(4)
    @DisplayName("잘못된 인증 코드로 인증 실패 테스트")
    void verifyEmailWithWrongCodeTest() {
        // Given
        VerificationRequestDTO request = VerificationRequestDTO.builder()
            .email(TEST_EMAIL)
            .code("000000")  // 잘못된 인증 코드
            .build();

        try {
            // When
            restTemplate.postForEntity(
                createURLWithPort("/api/users/email/verification"),
                request,
                ApiRes.class
            );
        } catch (RestClientResponseException e) {
            // Then
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            assertTrue(e.getResponseBodyAsString().contains(ErrorCode.VERIFICATION_CODE_NOT_FOUND.getMessage()));
        }
    }

    @Test
    @Order(5)
    @DisplayName("만료된 인증 코드로 인증 실패 테스트")
    void verifyEmailWithExpiredCodeTest() {
        // Given
        EmailVerification expiredVerification = new EmailVerification(
            TEST_EMAIL + "expired",
            "123456",
            LocalDateTime.now().minusHours(1)  // 1시간 전 만료
        );
        emailVerificationRepository.save(expiredVerification);

        VerificationRequestDTO request = VerificationRequestDTO.builder()
            .email(TEST_EMAIL)
            .code("123456")
            .build();

        try {
            // When
            restTemplate.postForEntity(
                createURLWithPort("/api/users/email/verification"),
                request,
                ApiRes.class
            );
        } catch (RestClientResponseException e) {
            // Then
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            assertTrue(e.getResponseBodyAsString().contains(ErrorCode.VERIFICATION_CODE_EXPIRED.getMessage()));
        }
    }

    @Test
    @Order(6)
    @DisplayName("이미 인증된 코드로 재인증 시도 테스트")
    void verifyAlreadyVerifiedEmailTest() {
        // Given
        EmailVerification verifiedEmail = new EmailVerification(
            TEST_EMAIL + "verified",
            "123456",
            LocalDateTime.now().plusHours(1)
        );
        verifiedEmail.verify();
        emailVerificationRepository.save(verifiedEmail);

        VerificationRequestDTO request = VerificationRequestDTO.builder()
            .email(TEST_EMAIL + "verified")
            .code("123456")
            .build();

        try {
            // When
            restTemplate.postForEntity(
                createURLWithPort("/api/users/email/verification"),
                request,
                ApiRes.class
            );
        } catch (RestClientResponseException e) {
            // Then
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            assertTrue(e.getResponseBodyAsString().contains(ErrorCode.VERIFICATION_ALREADY_VERIFIED.getMessage()));
        }
    }
}
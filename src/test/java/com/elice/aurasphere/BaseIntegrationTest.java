package com.elice.aurasphere;

import com.elice.aurasphere.global.common.ApiRes;
import com.elice.aurasphere.user.dto.LoginRequestDTO;
import com.elice.aurasphere.user.dto.TokenInfoDTO;
import com.elice.aurasphere.user.entity.Profile;
import com.elice.aurasphere.user.entity.User;
import com.elice.aurasphere.user.repository.ProfileRepository;
import com.elice.aurasphere.user.repository.UserRepository;
import java.io.IOException;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.DefaultResponseErrorHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {
    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ProfileRepository profileRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @LocalServerPort
    protected int port;

    protected String accessToken;
    protected String refreshToken;
    protected static final String BASE_URL = "http://localhost:";
    protected User testUser;
    protected Profile testProfile;

    protected String createURLWithPort(String uri) {
        return BASE_URL + port + uri;
    }

    protected HttpHeaders createAuthorizationHeader() {
        HttpHeaders headers = new HttpHeaders();
        if (accessToken != null) {
            headers.add("Authorization", accessToken);
        }
        return headers;
    }

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        userRepository.deleteAll();
        profileRepository.deleteAll();

        // PATCH 메서드를 지원하는 RestTemplate 설정
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        restTemplate = new TestRestTemplate(
            new RestTemplateBuilder()
                .requestFactory(() -> requestFactory)
        ).withBasicAuth("", "");

        // ErrorHandler 설정
        restTemplate.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                HttpStatusCode statusCode = response.getStatusCode();
                return statusCode.is5xxServerError();
            }
        });
    }

    protected void createTestUser() {
        testUser = User.builder()
            .email("test@example.com")
            .password(passwordEncoder.encode("Password123!"))
            .role("USER")
            .build();

        testProfile = Profile.builder()
            .user(testUser)
            .nickname("testUser")
            .profileUrl("DEFAULT")
            .build();

        userRepository.save(testUser);
        profileRepository.save(testProfile);
    }

    protected void performLogin() {
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
            .email("test@example.com")
            .password("Password123!")
            .build();

        ResponseEntity<ApiRes> response = restTemplate.postForEntity(
            createURLWithPort("/api/login"),
            loginRequest,
            ApiRes.class
        );

        ObjectMapper mapper = new ObjectMapper();
        LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) response.getBody().getData();
        TokenInfoDTO tokenInfo = mapper.convertValue(map, TokenInfoDTO.class);

        accessToken = tokenInfo.getAccessToken();
        refreshToken = tokenInfo.getRefreshToken();
    }
}
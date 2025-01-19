package com.elice.aurasphere.config;

import com.elice.aurasphere.user.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider tokenProvider;
    private final CookieUtil cookieUtil;
    private final String REDIRECT_URI = "http://localhost:3000";
    private final String LOGIN_FAILURE_URI = "http://localhost:3000/login";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = extractEmail(oAuth2User);
            String provider = extractProvider(request.getRequestURI());

            // 이메일 미동의 사용자 처리
            if (email == null) {
                email = generateTempEmail(provider, oAuth2User);
                log.info("Generated temporary email for user without email consent: {}", email);
            }

            List<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toList());

            String accessToken = tokenProvider.createAccessToken(email, roles);
            String refreshToken = tokenProvider.createRefreshToken(email);

            // 토큰을 쿠키에 저장
            cookieUtil.addAccessTokenCookie(response, accessToken, tokenProvider.ACCESS_TOKEN_VALIDITY);
            cookieUtil.addRefreshTokenCookie(response, refreshToken, tokenProvider.REFRESH_TOKEN_VALIDITY);

            // 프론트엔드로 리다이렉트
            getRedirectStrategy().sendRedirect(request, response, REDIRECT_URI);

        } catch (Exception ex) {
            log.error("OAuth2 authentication failed", ex);
            handleAuthenticationFailure(response, ex.getMessage());
        }
    }

    private String extractEmail(OAuth2User oAuth2User) {
        String email = null;

        // Kakao
        Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
        if (kakaoAccount != null && (Boolean) kakaoAccount.get("has_email")) {
            email = (String) kakaoAccount.get("email");
        }

        // Naver
        Map<String, Object> naverResponse = oAuth2User.getAttribute("response");
        if (naverResponse != null) {
            email = (String) naverResponse.get("email");
        }

        return email;
    }

    private String extractProvider(String requestUri) {
        if (requestUri.contains("kakao")) {
            return "KAKAO";
        } else if (requestUri.contains("naver")) {
            return "NAVER";
        }
        return "UNKNOWN";
    }

    private String generateTempEmail(String provider, OAuth2User oAuth2User) {
        String id = "";
        if (provider.equals("KAKAO")) {
            id = String.valueOf(oAuth2User.getAttribute("id"));
        } else if (provider.equals("NAVER")) {
            Map<String, Object> response = oAuth2User.getAttribute("response");
            id = (String) response.get("id");
        }

        return String.format("%s_%s@temp.%s.com",
            provider.toLowerCase(),
            id,
            UUID.randomUUID().toString().substring(0, 8));
    }

    private void handleAuthenticationFailure(HttpServletResponse response, String errorMessage)
        throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse errorResponse = new ErrorResponse(
            errorMessage,
            "AUTH_FAILURE"
        );

        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }
}
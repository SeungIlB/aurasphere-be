package com.elice.aurasphere.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final String REDIRECT_URI = "http://localhost:3000/login"; // 로그인 페이지 URI

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException exception) throws IOException, ServletException {

        log.error("소셜 로그인 실패: {}", exception.getMessage());

        String targetUrl = REDIRECT_URI +
            "?error=" + URLEncoder.encode(exception.getLocalizedMessage(), StandardCharsets.UTF_8);

        response.sendRedirect(targetUrl);
    }
}
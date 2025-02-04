package com.elice.aurasphere.global.oauth2;

import com.elice.aurasphere.global.common.ApiRes;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException exception) throws IOException {

        log.error("소셜 로그인 실패: {}", exception.getMessage());

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiRes<Void> errorResponse = ApiRes.failureRes(
            HttpStatus.UNAUTHORIZED,
            "소셜 로그인에 실패했습니다: " + exception.getMessage(),
            null
        );

        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }
}
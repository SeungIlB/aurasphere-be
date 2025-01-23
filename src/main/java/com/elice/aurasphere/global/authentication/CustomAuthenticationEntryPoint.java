package com.elice.aurasphere.global.authentication;

import com.elice.aurasphere.global.common.ApiRes;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {

        String errorMessage;

        if (authException instanceof BadCredentialsException) {
            errorMessage = "이메일 또는 비밀번호가 일치하지 않습니다.";
        } else {
            errorMessage = "인증이 필요한 엔드포인트입니다.";
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");


        ApiRes<Void> errorResponse = ApiRes.failureRes(
            HttpStatus.UNAUTHORIZED,
            errorMessage,
            null
        );

        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }
}

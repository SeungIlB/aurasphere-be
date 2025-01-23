package com.elice.aurasphere.global.authentication;


import com.elice.aurasphere.global.common.ApiRes;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
        AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        ApiRes<Void> errorResponse = ApiRes.failureRes(
            HttpStatus.FORBIDDEN,
            "해당 리소스에 대한 권한이 없습니다.",
            null
        );

        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }
}
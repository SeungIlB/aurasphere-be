package com.elice.aurasphere.global.filter;

import com.elice.aurasphere.user.dto.ErrorResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtExceptionFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            setErrorResponse(response, e);
        }
    }

    private void setErrorResponse(HttpServletResponse response, JwtException e) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(e.getMessage(), "JWT_ERROR");
        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }
}
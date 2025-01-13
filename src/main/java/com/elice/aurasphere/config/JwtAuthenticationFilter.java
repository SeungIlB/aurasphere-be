package com.elice.aurasphere.config;

import com.elice.aurasphere.user.dto.ErrorResponse;
import com.elice.aurasphere.user.dto.TokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    // 인증(로그인)없어도 접근 가능한 리소스
    private final List<String> EXCLUDED_URLS = Arrays.asList(
        "/login",
        "/signup",
        "/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        log.debug("Request URI: " + request.getRequestURI());
        log.debug("Should Not Filter: " + shouldNotFilter(request));

        String accessToken = resolveToken(request);

        // 액세스 토큰이 없다면 예외 발생
        if (accessToken == null) {
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다.");
            return;
        }

        try {
            // 토큰 유효성 검사 및 인증 처리
            if (jwtTokenProvider.validateToken(accessToken)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            // Access Token이 만료된 경우 자동으로 재발급
            reIssueAccessToken(request, response);
        } catch (SignatureException e) {
            setErrorResponse(response, HttpStatus.BAD_REQUEST, "변조된 토큰입니다.");
            return;
        } catch (Exception e) {
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void reIssueAccessToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 리프레시 토큰으로 새로운 액세스 토큰 발급
            TokenResponse tokenResponse = jwtTokenProvider.reIssueAccessToken(
                request.getHeader("Refresh-Token")  // 리프레시 토큰은 별도 헤더로 받음
            );

            // 새로운 토큰으로 인증 처리
            Authentication authentication = jwtTokenProvider.getAuthentication(tokenResponse.getAccessToken());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 응답 헤더에 새로운 토큰 정보 추가
            response.setHeader("New-Access-Token", tokenResponse.getAccessToken());
            response.setHeader("New-Refresh-Token", tokenResponse.getRefreshToken());

        } catch (Exception e) {
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "토큰 재발급 실패");
            throw e;
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected  boolean shouldNotFilter(HttpServletRequest request) {
        return EXCLUDED_URLS.stream()
            .anyMatch(exclude -> request.getRequestURI().equals(exclude) ||
                request.getRequestURI().startsWith(exclude));
    }

    private void setErrorResponse(HttpServletResponse response, HttpStatus status, String message)
        throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse errorResponse = new ErrorResponse(message);
        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }
}

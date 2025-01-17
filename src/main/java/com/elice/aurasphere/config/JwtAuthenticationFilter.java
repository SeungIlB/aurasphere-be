package com.elice.aurasphere.config;


import com.elice.aurasphere.config.CookieUtil;
import com.elice.aurasphere.user.dto.ErrorResponse;
import com.elice.aurasphere.user.dto.TokenInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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
    private final CookieUtil cookieUtil;

    // 인증(로그인)없어도 접근 가능한 리소스
    private final List<String> EXCLUDED_URLS = Arrays.asList(
        "/login",
        "/signup",
        "/swagger-ui",  // Swagger UI 경로
        "/v3/api-docs", // OpenAPI 문서 경로
        "/swagger-ui.html"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        // 요청 정보 상세 로깅
        log.info("========== Request Details ==========");
        log.info("Request Method: {}", request.getMethod());
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Request URL: {}", request.getRequestURL());
        log.info("Servlet Path: {}", request.getServletPath());

        String accessToken = getTokenFromCookie(request, cookieUtil.ACCESS_TOKEN_COOKIE_NAME);
        log.info("Access token from cookie: {}", accessToken != null ? "present" : "null");

        // 액세스 토큰이 없다면 예외 발생
        if (accessToken == null) {
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다.");
            return;
        }

        try {
            // 토큰 유효성 검사 및 인증 처리
            if (jwtTokenProvider.validateToken(accessToken)) {
                log.info("Token validation successful");
                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Authentication set in SecurityContext");
            }
        } catch (ExpiredJwtException e) {
            // Access Token이 만료된 경우 자동으로 재발급
            log.info("Token expired, attempting to refresh");
            reIssueAccessToken(request, response);
        } catch (SignatureException e) {
            log.error("Token validation failed", e);
            setErrorResponse(response, HttpStatus.BAD_REQUEST, "변조된 토큰입니다.");
            return;
        } catch (Exception e) {
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void reIssueAccessToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String refreshToken = getTokenFromCookie(request, cookieUtil.REFRESH_TOKEN_COOKIE_NAME);

        if (refreshToken == null) {
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "Refresh 토큰이 존재하지 않습니다.");
            return;
        }

        try {
            TokenInfo tokenInfo = jwtTokenProvider.reIssueAccessToken(refreshToken);

            cookieUtil.addAccessTokenCookie(response, tokenInfo.getAccessToken(),
                jwtTokenProvider.REFRESH_TOKEN_VALIDITY);
            cookieUtil.addRefreshTokenCookie(response, tokenInfo.getRefreshToken(),
                jwtTokenProvider.REFRESH_TOKEN_VALIDITY);

            Authentication authentication = jwtTokenProvider.getAuthentication(tokenInfo.getAccessToken());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "토큰 재발급에 실패했습니다.");
        }
    }

    private String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        boolean shouldNotFilter = EXCLUDED_URLS.stream()
            .anyMatch(exclude -> request.getRequestURI().equals(exclude) ||
                request.getRequestURI().startsWith(exclude));
        log.info("URI: {} shouldNotFilter: {}", requestURI, shouldNotFilter);
        return shouldNotFilter;
    }

    private void setErrorResponse(HttpServletResponse response, HttpStatus status, String message)
        throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse errorResponse = new ErrorResponse(message);
        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }

//    private String resolveToken(HttpServletRequest request) {
//        String bearerToken = request.getHeader("Authorization");
//        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
//            return bearerToken.substring(7);
//        }
//        return null;
//    }
}

package com.elice.aurasphere.global.filter;

//import com.elice.aurasphere.global.utils.CookieUtil;
import com.elice.aurasphere.global.authentication.JwtTokenProvider;
import com.elice.aurasphere.global.common.ApiRes;
import com.elice.aurasphere.user.dto.TokenInfoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
//import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
//    private final CookieUtil cookieUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        // 요청 정보 상세 로깅
        log.info("========== Request Details ==========");
        log.info("Request Method: {}", request.getMethod());
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Request URL: {}", request.getRequestURL());
        log.info("Servlet Path: {}", request.getServletPath());

        String accessToken = request.getHeader("Authorization");
        log.info("Token from Authorization header: {}", accessToken != null ? "present" : "null");


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

//        // Authorization 헤더에서 토큰 추출
//        String bearerToken = request.getHeader("Authorization");
//        log.info("bearerToken: {}", bearerToken);
//        if (bearerToken != null) {
//            try {
//                if (jwtTokenProvider.validateToken(bearerToken)) {
//                    log.info("Token validation successful");
//                    Authentication authentication = jwtTokenProvider.getAuthentication(bearerToken);
//                    SecurityContextHolder.getContext().setAuthentication(authentication);
//                    log.info("Authentication set in SecurityContext");
//                }
//            } catch (Exception e) {
//                log.error("Token validation failed", e);
//            }
//        }

        filterChain.doFilter(request, response);
    }

    private void reIssueAccessToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String refreshToken = request.getHeader("refreshToken");

        if (refreshToken == null) {
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "Refresh 토큰이 존재하지 않습니다.");
            return;
        }

        try {
            TokenInfoDTO tokenInfo = jwtTokenProvider.reIssueAccessToken(refreshToken);

            // 새로운 토큰들을 응답 헤더에 설정
            response.setHeader("Authorization", tokenInfo.getAccessToken());
            response.setHeader("refreshToken", tokenInfo.getRefreshToken());

            Authentication authentication = jwtTokenProvider.getAuthentication(tokenInfo.getAccessToken());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "토큰 재발급에 실패했습니다.");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/login") ||
            path.startsWith("/api/signup") ||
            path.startsWith("/api/oauth2") ||
            path.startsWith("/api/users/nickname") ||
            path.startsWith("/api/user/reset_password") ||
            path.startsWith("/api/users/password/verification_code") ||
            path.startsWith("/api/users/email/verification_code") ||
            path.startsWith("/api/users/email/verification") ||
            path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/swagger-ui.html");
    }

    private void setErrorResponse(HttpServletResponse response, HttpStatus status, String message)
        throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");

        ApiRes<Void> errorResponse = ApiRes.failureRes(
            status,
            message,
            null
        );

        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }

//    private String getTokenFromCookie(HttpServletRequest request, String cookieName) {
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            for (Cookie cookie : cookies) {
//                if (cookieName.equals(cookie.getName())) {
//                    return cookie.getValue();
//                }
//            }
//        }
//        return null;
//    }
}

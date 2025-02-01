package com.elice.aurasphere.global.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CookieUtil {
    public final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    public final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    public void addAccessTokenCookie(HttpServletResponse response, String token, long maxAge) {
        Cookie cookie = createCookie(ACCESS_TOKEN_COOKIE_NAME, token, maxAge);
        response.addCookie(cookie);
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String token, long maxAge) {
        Cookie cookie = createCookie(REFRESH_TOKEN_COOKIE_NAME, token, maxAge);
        response.addCookie(cookie);
    }

    private Cookie createCookie(String name, String value, long maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "None");
        cookie.setMaxAge((int) (maxAge / 1000)); // milliseconds to seconds
        return cookie;
    }

    public void deleteAccessTokenCookie(HttpServletResponse response) {
        log.info("Deleting access token cookie");
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        log.info("Access token cookie deletion completed");
    }

    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        log.info("Deleting refresh token cookie");
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        log.info("Refresh token cookie deletion completed");
    }
}

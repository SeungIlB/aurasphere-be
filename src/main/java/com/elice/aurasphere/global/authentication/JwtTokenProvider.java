package com.elice.aurasphere.global.authentication;

import com.elice.aurasphere.user.service.CustomUserDetailsService;
import com.elice.aurasphere.user.dto.TokenInfoDTO;
import com.elice.aurasphere.user.entity.RefreshToken;
import com.elice.aurasphere.user.repository.RefreshTokenRepository;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomUserDetailsService userDetailsService;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-validity-in-seconds}")
    public long ACCESS_TOKEN_VALIDITY;// 30분

    @Value("${jwt.refresh-token-validity-in-seconds}")
    public long REFRESH_TOKEN_VALIDITY;    // 7일

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createAccessToken(String userEmail, List<String> role) {
        Claims claims = Jwts.claims().setSubject(userEmail);
        claims.put("role", role);

        Date now = new Date();
        Date validity = new Date(now.getTime() + ACCESS_TOKEN_VALIDITY);

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(String userEmail) {
        Claims claims = Jwts.claims().setSubject(userEmail);

        Date now = new Date();
        Date validity = new Date(now.getTime() + REFRESH_TOKEN_VALIDITY);

        String refreshToken = Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();

        saveRefreshToken(userEmail, refreshToken);
        return refreshToken;
    }

    // Refresh Token 저장
    private void saveRefreshToken(String userEmail, String refreshToken) {
        refreshTokenRepository.findByUsername(userEmail)
            .ifPresentOrElse(
                token -> token.updateToken(refreshToken, java.time.LocalDateTime.now().plusDays(7)),
                () -> refreshTokenRepository.save(new RefreshToken(
                    userEmail, refreshToken, java.time.LocalDateTime.now().plusDays(7)))
            );
    }

    // 토큰에서 인증 정보 조회
    public Authentication getAuthentication(String token) {
        String userEmail = getUserEmail(token);
        log.info("userEmail: {}", userEmail);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

//    public Authentication getAuthentication(String email) {
//        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
//        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
//    }

    // 토큰에서 사용자 이메일 추출
    public String getUserEmail(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String refreshToken) {
        try {
            log.info("bearerToken: {}", refreshToken);
            Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(refreshToken);

            log.info("Token expiration date: {}", claims.getBody().getExpiration());
            log.info("Current date: {}", new Date());
            log.info("Is token expired: {}", claims.getBody().getExpiration().before(new Date()));

            return !claims.getBody().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            log.warn("Token has expired", e);
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token", e);
            return false;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token", e);
            return false;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature", e);
            return false;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty", e);
            return false;
        }
    }

    //토큰 재발급
    public TokenInfoDTO reIssueAccessToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String userEmail = getUserEmail(refreshToken);
        Authentication authentication = getAuthentication(refreshToken);

        List<String> role = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        String newAccessToken = createAccessToken(userEmail, role);
        String newRefreshToken = createRefreshToken(userEmail);

        return new TokenInfoDTO(
            "Bearer",
            newAccessToken,
            newRefreshToken,
            ACCESS_TOKEN_VALIDITY
        );
    }
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        return claims.get("userId", Long.class); // JWT의 "userId" 클레임을 가져옴
    }
}
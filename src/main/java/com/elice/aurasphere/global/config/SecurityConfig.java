package com.elice.aurasphere.global.config;

import com.elice.aurasphere.global.oauth2.OAuth2AuthenticationFailureHandler;
import com.elice.aurasphere.global.oauth2.OAuth2AuthenticationSuccessHandler;
import com.elice.aurasphere.global.authentication.CustomAccessDeniedHandler;
import com.elice.aurasphere.global.authentication.CustomAuthenticationEntryPoint;
import com.elice.aurasphere.global.authentication.JwtTokenProvider;
import com.elice.aurasphere.global.filter.JwtAuthenticationFilter;
import com.elice.aurasphere.global.filter.JwtExceptionFilter;
import com.elice.aurasphere.global.oauth2.CustomOAuth2UserService;
import com.elice.aurasphere.global.utils.CookieUtil;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CookieUtil cookieUtil;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)  // csrf 비활성화
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .formLogin(AbstractHttpConfigurer::disable)  // formLogin 비활성화
            .httpBasic(AbstractHttpConfigurer::disable)  // httpBasic 비활성화
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .oauth2Login(oauth2 -> {
                oauth2.userInfoEndpoint(userInfo ->
                    userInfo.userService(customOAuth2UserService)
                );
                oauth2.successHandler(oauth2AuthenticationSuccessHandler);
                oauth2.failureHandler(oauth2AuthenticationFailureHandler);
            })

            .exceptionHandling(handling -> {
                handling
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler);
            })

            .authorizeHttpRequests(authorize -> {
                authorize
                    .requestMatchers( "/api/login", "/api/signup", "/api/oauth2/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",  "/api//users/nickname", "/api/users/email/verification_code", "/api/users/email/verification").permitAll()
                    .requestMatchers("/api/admin").hasRole("ADMIN")
                    .anyRequest().authenticated();
            })

            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, cookieUtil),
                UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new JwtExceptionFilter(),
                JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://34.64.75.50:5173", "http://localhost:8080")); // 프론트엔드 주소
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
        throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
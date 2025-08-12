package com.hollywood.sweetspot.security;

import org.springframework.context.annotation.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
public class SecurityConfig {
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService uds;
    private final RestAuthEntryPoint authEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final OAuth2UserServiceImpl oAuth2UserServiceImpl;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    public SecurityConfig(
            JwtUtils j,
            CustomUserDetailsService u,
            RestAuthEntryPoint ep,
            RestAccessDeniedHandler ad,
            OAuth2UserServiceImpl oAuth2UserServiceImpl, // ⬅️ 추가
            OAuth2SuccessHandler oAuth2SuccessHandler // ⬅️ 추가
    ) {
        this.jwtUtils = j;
        this.uds = u;
        this.authEntryPoint = ep;
        this.accessDeniedHandler = ad;
        this.oAuth2UserServiceImpl = oAuth2UserServiceImpl; // ⬅️
        this.oAuth2SuccessHandler = oAuth2SuccessHandler; // ⬅️
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        var cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        cfg.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        var jwtFilter = new JwtAuthenticationFilter(jwtUtils, uds);

        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(h -> h
                .authenticationEntryPoint(authEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/signin",
                    "/api/auth/signup",
                    "/api/auth/refresh",
                    "/oauth2/**",
                    "/login/oauth2/**",
                    "/api/auth/complete",
                    "/auth/google/start",
                    "/actuator/health"
                ).permitAll()
                .anyRequest().authenticated())
            // ✅ OAuth2 로그인 활성화 + 우리 서비스들 연결
            .oauth2Login(oauth -> oauth
                .userInfoEndpoint(ui -> ui.oidcUserService(oAuth2UserServiceImpl))
                .successHandler(oAuth2SuccessHandler)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
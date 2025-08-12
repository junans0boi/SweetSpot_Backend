// src/main/java/com/hollywood/sweetspot/security/JwtAuthenticationFilter.java
package com.hollywood.sweetspot.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtils j, CustomUserDetailsService u) {
        this.jwtUtils = j;
        this.userDetailsService = u;
    }

    // src/main/java/com/hollywood/sweetspot/security/JwtAuthenticationFilter.java
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String token = null;

        // 1) Authorization 헤더 우선
        String header = req.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        // 2) 없으면 SS_ACCESS 쿠키 사용
        if (token == null) {
            token = getCookieValue(req, "SS_ACCESS");
        }

        if (token != null && jwtUtils.validateJwt(token)) {
            String email = jwtUtils.getEmailFromJwt(token);
            UserDetails user = userDetailsService.loadUserByUsername(email);
            var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(req, res);
    }

    private String getCookieValue(HttpServletRequest req, String name) {
        var cookies = req.getCookies();
        if (cookies == null)
            return null;
        for (var c : cookies) {
            if (name.equals(c.getName()))
                return c.getValue();
        }
        return null;
    }
}
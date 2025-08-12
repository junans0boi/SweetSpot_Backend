package com.hollywood.sweetspot.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtils {
    @Value("${app.jwtSecret}")
    private String jwtSecret;
    @Value("${app.jwtExpirationMs}")
    private long jwtExpirationMs;

    private Key getKey() {
        // .env의 JWT_SECRET이 Base64라면 이렇게 decode
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        // 만약 평문(임의 문자열)이라면:
        // return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        var roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority()).collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getEmailFromJwt(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwt(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
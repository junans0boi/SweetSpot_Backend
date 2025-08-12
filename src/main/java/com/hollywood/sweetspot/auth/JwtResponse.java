package com.hollywood.sweetspot.auth;

// JWT 응답
public record JwtResponse(String token, String type, Long id, String email, String roles) {
}
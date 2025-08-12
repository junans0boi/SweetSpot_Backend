package com.hollywood.sweetspot.user;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
// @RestController : JSON 반환 컨트롤러.
@RestController
// @RequestMapping : 공통 URL 접두어.
@RequestMapping("/api/auth")
public class UserController {

    private final UserRepository userRepo;

    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }
	// Authentication 파라미터 : 시큐리티가 현재 로그인 사용자 정보를 주입.

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "UNAUTHORIZED", "message", "Authentication required"));
        }
        var email = authentication.getName();
        // ResponseEntity.ok :  : HTTP 200 + 바디.
        return ResponseEntity.ok(userRepo.findByEmail(email).orElseThrow());
    }
}
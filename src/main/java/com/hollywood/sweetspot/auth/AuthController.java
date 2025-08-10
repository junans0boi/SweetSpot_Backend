// src/main/java/com/hollywood/sweetspot/auth/AuthController.java
package com.hollywood.sweetspot.auth;

import com.hollywood.sweetspot.security.JwtUtils;
import com.hollywood.sweetspot.user.User;
import com.hollywood.sweetspot.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager a, UserRepository r, PasswordEncoder e, JwtUtils j) {
        this.authManager = a;
        this.userRepo = r;
        this.encoder = e;
        this.jwtUtils = j;
    }

    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest req) {
        if (userRepo.existsByEmail(req.email())) {
            return "Error: Email already in use";
        }
        User user = User.builder()
                .email(req.email())
                .password(encoder.encode(req.password()))
                .roles("USER")
                .build();
        userRepo.save(user);
        return "User registered";
    }

    @PostMapping("/signin")
    public JwtResponse signin(@RequestBody LoginRequest req) {
        try {
            // 1) 인증 시도
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password()));

            // 2) SecurityContext 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3) 인증된 username(email)로 실제 User 엔티티 로드
            String email = authentication.getName();
            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            // 4) JWT 생성 후 응답
            String token = jwtUtils.generateToken(authentication);
            return new JwtResponse(token, "Bearer", user.getId(), user.getEmail(), user.getRoles());

        } catch (AuthenticationException ex) {
            // 인증 실패 → 401 로 상세 원인 노출 (개발/디버그용)
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    ex.getClass().getSimpleName() + ": " + ex.getMessage()
            );
        }
    }
}
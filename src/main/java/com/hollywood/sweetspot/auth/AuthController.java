package com.hollywood.sweetspot.auth;

import com.hollywood.sweetspot.user.User;
import com.hollywood.sweetspot.user.UserRepository;
import com.hollywood.sweetspot.security.JwtUtils;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshSvc;
    @Value("${app.oauth2.cookie.secure:false}")
    boolean cookieSecure;
    @Value("${app.oauth2.cookie.domain:}")
    String cookieDomain;

    public AuthController(AuthenticationManager a, UserRepository r, PasswordEncoder e, JwtUtils j,
            RefreshTokenService rs) {
        this.authManager = a;
        this.userRepo = r;
        this.encoder = e;
        this.jwtUtils = j;
        this.refreshSvc = rs;
    }

    public record JwtWithRefresh(String token, String type, Long id, String email, String roles, String refreshToken) {
    }

    @PostMapping("/signup")
    public Map<String, String> signup(@RequestBody SignupRequest req) {
        if (userRepo.existsByEmail(req.email())) {
            return Map.of("status", "ERROR", "message", "Email already in use");
        }
        // roles는 "USER" 형태(접두사 X) 권장
        User user = User.builder()
                .email(req.email())
                .password(encoder.encode(req.password()))
                .roles("USER")
                .name(req.name())
                .build();
        userRepo.save(user);
        return Map.of("status", "OK", "message", "User registered");
    }

    @PostMapping("/signin")
    public JwtWithRefresh signin(@RequestBody LoginRequest req) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String email = authentication.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        String access = jwtUtils.generateToken(authentication);
        String refresh = refreshSvc.createForUser(user.getId(), 30);

        return new JwtWithRefresh(access, "Bearer", user.getId(), user.getEmail(), user.getRoles(), refresh);
    }

    @PostMapping("/refresh")
    public JwtWithRefresh refresh(@RequestBody Map<String, String> body) {
        String raw = body.get("refreshToken");
        var rt = refreshSvc.verify(raw);
        var user = userRepo.findById(rt.getUserId()).orElseThrow();

        var auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null,
                org.springframework.security.core.authority.AuthorityUtils
                        .commaSeparatedStringToAuthorityList(
                                // roles 문자열을 "USER,ADMIN" -> "ROLE_USER,ROLE_ADMIN" 로 바꿔도 되지만
                                // 아래처럼 UserDetails 없이 인증 토큰을 만들 때는 간단히 빈 권한으로 발급해도 무방.
                                user.getRoles()));
        String newAccess = jwtUtils.generateToken(auth);
        String newRefresh = refreshSvc.rotate(rt, user.getId(), 30);

        return new JwtWithRefresh(newAccess, "Bearer", user.getId(), user.getEmail(), user.getRoles(), newRefresh);
    }

    @PostMapping("/logout")
    public Map<String, String> logout(Authentication auth, HttpServletResponse res) {
        var email = auth.getName();
        var user = userRepo.findByEmail(email).orElseThrow();
        refreshSvc.revokeAllForUser(user.getId());
        SecurityContextHolder.clearContext();

        // 쿠키 제거
        deleteCookie(res, "SS_ACCESS");
        deleteCookie(res, "SS_REFRESH");

        return Map.of("status", "OK", "message", "Logged out");
    }

    private void deleteCookie(HttpServletResponse res, String name) {
        var c = new jakarta.servlet.http.Cookie(name, "");
        c.setPath("/");
        c.setMaxAge(0);
        c.setHttpOnly(true);
        c.setSecure(cookieSecure);
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            c.setDomain(cookieDomain); // ← 설정 때와 동일해야 삭제됨
        }
        res.addCookie(c);
    }
}
package com.hollywood.sweetspot.auth;

import com.hollywood.sweetspot.user.UserRepository;
import com.hollywood.sweetspot.common.MailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/auth/password")
public class PasswordController {
    private final UserRepository users;
    private final PasswordResetService resets;
    private final PasswordEncoder encoder;
    private final MailService mail;
    @Value("${app.front.base-url}")
    String frontBaseUrl;

    public PasswordController(UserRepository u, PasswordResetService r, PasswordEncoder e, MailService m) {
        this.users = u;
        this.resets = r;
        this.encoder = e;
        this.mail = m;
    }

    // 비밀번호 재설정 링크 요청 (항상 200으로 응답 → 계정 존재 여부 노출 방지)
    @PostMapping("/reset-request")
    public Map<String, String> request(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "");
        users.findByEmail(email).ifPresent(u -> {
            String token = resets.issue(email, 30); // 30분 유효
                        String url = frontBaseUrl + "/reset?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
            mail.sendPasswordReset(email, url);
        });
        return Map.of("status", "OK");
    }

    // 토큰으로 실제 비밀번호 변경
    @PostMapping("/reset")
    public ResponseEntity<?> reset(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPw = body.get("newPassword");
        if (token == null || newPw == null)
            return ResponseEntity.badRequest().build();

        String email = resets.verify(token);
        var user = users.findByEmail(email).orElseThrow();
        user.setPassword(encoder.encode(newPw));
        users.save(user);
        return ResponseEntity.ok(Map.of("status", "OK"));
    }
}
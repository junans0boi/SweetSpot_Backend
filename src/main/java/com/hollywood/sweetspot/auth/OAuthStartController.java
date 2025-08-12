// src/main/java/com/hollywood/sweetspot/auth/OAuthStartController.java
package com.hollywood.sweetspot.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OAuthStartController {

    @GetMapping("/auth/google/start")
    public String start(@RequestParam(defaultValue = "web") String mode, HttpServletResponse res) {
        // 모드 쿠키 저장 (Lax면 같은 사이트 내 리다이렉트에서 자동 전송)
        Cookie c = new Cookie("AUTH_MODE", mode);
        c.setHttpOnly(false);
        c.setSecure(false); // 운영은 true
        c.setPath("/");
        c.setMaxAge(300); // 5분
        res.addCookie(c);

        // 표준 스프링 엔드포인트로 이동
        return "redirect:/oauth2/authorization/google";
    }
}
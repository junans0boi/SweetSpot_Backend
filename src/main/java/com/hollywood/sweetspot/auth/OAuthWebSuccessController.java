// src/main/java/com/hollywood/sweetspot/auth/OAuthWebSuccessController.java
package com.hollywood.sweetspot.auth;

import com.hollywood.sweetspot.security.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OAuthWebSuccessController {
    private final JwtUtils jwt;
    public OAuthWebSuccessController(JwtUtils jwt){ this.jwt = jwt; }

    @GetMapping(value="/oauth/success", produces = MediaType.TEXT_HTML_VALUE)
    public String success(HttpServletRequest req, org.springframework.ui.Model model) {
        String access = null;
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("SS_ACCESS".equals(c.getName())) {
                    access = c.getValue();
                    break;
                }
            }
        }
        String email = (access != null && jwt.validateJwt(access)) ? jwt.getEmailFromJwt(access) : "(알 수 없음)";
        model.addAttribute("email", email);
        // 간단한 템플릿 없이 문자열 리턴도 가능하지만, 여기서는 뷰 없이 정적 HTML로 빠르게:
        return "forward:/oauth/success.html";
    }
}
// src/main/java/com/hollywood/sweetspot/security/OAuth2SuccessHandler.java
package com.hollywood.sweetspot.security;

import com.hollywood.sweetspot.auth.RefreshTokenService;
import com.hollywood.sweetspot.user.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler
        implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {
    private final JwtUtils jwt;
    private final RefreshTokenService refresh;
    private final UserRepository users;
    private final TicketStore ticketStore;

    @Value("${app.oauth2.redirect.web}")
    String webRedirect;

    @Value("${app.oauth2.redirect.mobile}")
    String mobileRedirect;

    // 🔽 추가: 프로필로 쿠키 보안 플래그/도메인 제어
    @Value("${app.oauth2.cookie.secure:true}")
    boolean cookieSecure;

    @Value("${app.oauth2.cookie.domain:}")
    String cookieDomain;

    public OAuth2SuccessHandler(JwtUtils j, RefreshTokenService r, UserRepository u, TicketStore t) {
        this.jwt = j;
        this.refresh = r;
        this.users = u;
        this.ticketStore = t;
    }

    private String readModeFromCookie(HttpServletRequest req) {
        if (req.getCookies() == null)
            return "web";
        for (var c : req.getCookies()) {
            if ("AUTH_MODE".equals(c.getName()))
                return c.getValue();
        }
        return "web";
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth)
            throws IOException {
        var oidc = (OidcUser) auth.getPrincipal();
        var email = oidc.getEmail();
        var user = users.findByEmail(email).orElseThrow();

        var springAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                email, null, org.springframework.security.core.authority.AuthorityUtils
                        .commaSeparatedStringToAuthorityList(user.getRoles()));
        String access = jwt.generateToken(springAuth);
        String refreshToken = refresh.createForUser(user.getId(), 30);

        // 🔑 여기! state 대신 쿠키로 판별
        String mode = readModeFromCookie(req);

        if ("web".equalsIgnoreCase(mode)) {
            addCookie(res, "SS_ACCESS", access, true);
            addCookie(res, "SS_REFRESH", refreshToken, true);
            res.sendRedirect(webRedirect);
        } else {
            String ticket = storeTicket(access, refreshToken);
            String deeplink = UriComponentsBuilder.fromUriString(mobileRedirect)
                    .queryParam("ticket", ticket)
                    .build().toUriString();
            res.sendRedirect(deeplink);
        }
    }

    private void addCookie(HttpServletResponse res, String name, String val, boolean httpOnly) {
        var c = new Cookie(name, val);
        c.setHttpOnly(httpOnly);
        c.setSecure(cookieSecure); // 🔸 dev에서는 false, prod에서는 true
        c.setPath("/");
        c.setMaxAge(60 * 60); // 1h
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            c.setDomain(cookieDomain);
        }
        res.addCookie(c);
    }

    private String storeTicket(String access, String refresh) {
        return ticketStore.put(access, refresh, 60); // TTL 60s
    }
}
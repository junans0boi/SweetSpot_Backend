// src/main/java/com/hollywood/sweetspot/security/OAuth2UserServiceImpl.java
package com.hollywood.sweetspot.security;

import com.hollywood.sweetspot.user.User;
import com.hollywood.sweetspot.user.UserRepository;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.*;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.UUID;

@Service
public class OAuth2UserServiceImpl extends OidcUserService {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public OAuth2UserServiceImpl(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest req) {
        OidcUser oidcUser = super.loadUser(req);
        Map<String, Object> attr = oidcUser.getAttributes();
        // 표준 OIDC
        String sub = (String) attr.get("sub");
        String email = (String) attr.get("email");
        String name  = (String) attr.getOrDefault("name", email);
        String picture = (String) attr.getOrDefault("picture", null);

        // find-or-create
        var user = userRepo.findByEmail(email).orElseGet(() -> {
            return userRepo.save(User.builder()
                    .email(email)
                    .password(encoder.encode("SOCIAL-" + UUID.randomUUID())) // 혹은 null 허용
                    .roles("USER")
                    .build());
        });

        // 소셜 정보 업데이트(최초/변경 동기화)
        user.setProvider("GOOGLE");
        user.setRoles(user.getRoles() == null ? "USER" : user.getRoles());
        user.setPassword(user.getPassword()); // 유지
        user.setEmail(email);
        user.setName(name);
        user.setPictureUrl(picture);
        // providerId 저장하려면 엔티티에 필드 추가
        // user.setProviderId(sub);
        userRepo.save(user);

        return oidcUser; // SecurityContext에 OidcUser로 적재
    }
}
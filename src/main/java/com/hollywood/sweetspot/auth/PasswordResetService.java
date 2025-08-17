package com.hollywood.sweetspot.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
public class PasswordResetService {
    private final PasswordResetTokenRepository repo;
    private final PasswordEncoder encoder;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(PasswordResetTokenRepository r, PasswordEncoder e){
        this.repo = r; this.encoder = e;
    }

    public String issue(String email, int minutes){
        String raw = rawToken(48);
        String tokenId = UUID.randomUUID().toString();
        String hash = encoder.encode(raw);
        repo.save(PasswordResetToken.builder()
                .email(email)
                .tokenId(tokenId)
                .tokenHash(hash)
                .expiresAt(Instant.now().plus(minutes, ChronoUnit.MINUTES))
                .used(false).build());
        return tokenId + "." + raw;
    }

    public String verify(String presented){
        int dot = presented.indexOf('.');
        if (dot < 1) throw new RuntimeException("Malformed token");
        String tokenId = presented.substring(0, dot);
        String raw = presented.substring(dot+1);

        var prt = repo.findByTokenIdAndUsedFalse(tokenId)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (prt.getExpiresAt().isBefore(Instant.now()))
            throw new RuntimeException("Expired token");
        if (!encoder.matches(raw, prt.getTokenHash()))
            throw new RuntimeException("Invalid token");
        prt.setUsed(true);
        repo.save(prt);
        return prt.getEmail();
    }

    private String rawToken(int bytes){
        byte[] buf = new byte[bytes];
        random.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
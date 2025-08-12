package com.hollywood.sweetspot.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository repo;
    private final PasswordEncoder encoder;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public String createForUser(Long userId, int days) {
        String raw = generateRaw(48); // URL-safe Base64(점(.) 안 들어감)
        String hash = encoder.encode(raw);
        String tokenId = UUID.randomUUID().toString();

        repo.save(RefreshToken.builder()
                .userId(userId)
                .tokenId(tokenId)
                .tokenHash(hash)
                .expiresAt(Instant.now().plus(days, ChronoUnit.DAYS))
                .revoked(false)
                .build());

        return tokenId + "." + raw; // 클라엔트로는 이렇게 전달
    }

    public RefreshToken verify(String presented) {
        int dot = presented.indexOf('.');
        if (dot < 1)
            throw new RuntimeException("Malformed token");
        String tokenId = presented.substring(0, dot);
        String raw = presented.substring(dot + 1);

        var rt = repo.findByTokenIdAndRevokedFalse(tokenId)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (rt.getExpiresAt().isBefore(Instant.now()))
            throw new RuntimeException("Expired token");
        if (!encoder.matches(raw, rt.getTokenHash()))
            throw new RuntimeException("Invalid token");
        return rt;
    }

    public String rotate(RefreshToken oldToken, Long userId, int days) {
        oldToken.setRevoked(true);
        repo.save(oldToken);
        return createForUser(userId, days);
    }

    public void revokeAllForUser(Long userId) {
        repo.findAll().stream()
                .filter(rt -> rt.getUserId().equals(userId) && !rt.isRevoked())
                .forEach(rt -> {
                    rt.setRevoked(true);
                    repo.save(rt);
                });
    }

    private String generateRaw(int bytes) {
        byte[] buf = new byte[bytes];
        random.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
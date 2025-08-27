package com.hollywood.sweetspot.ugc;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hollywood.sweetspot.user.UserRepository;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteRepository repo;
    private final UserRepository users;

    record FavoriteReq(String provider, String externalId) {}

    @PostMapping
    public ResponseEntity<?> add(org.springframework.security.core.Authentication auth,
                                 @RequestBody FavoriteReq req) {
        var email = auth.getName();
        var user = users.findByEmail(email).orElseThrow();
        var exists = repo.findByUserIdAndProviderAndExternalId(user.getId(), req.provider(), req.externalId());
        if (exists.isPresent()) return ResponseEntity.ok(Map.of("status","OK"));
        repo.save(Favorite.builder()
                .userId(user.getId())
                .provider(req.provider())
                .externalId(req.externalId())
                .build());
        return ResponseEntity.ok(Map.of("status","OK"));
    }

    @DeleteMapping
    public ResponseEntity<?> remove(org.springframework.security.core.Authentication auth,
                                    @RequestParam String provider, @RequestParam String externalId) {
        var email = auth.getName();
        var user = users.findByEmail(email).orElseThrow();
        repo.deleteByUserIdAndProviderAndExternalId(user.getId(), provider, externalId);
        return ResponseEntity.ok(Map.of("status","OK"));
    }

    @GetMapping
    public List<Favorite> myFavorites(org.springframework.security.core.Authentication auth) {
        var email = auth.getName();
        var user = users.findByEmail(email).orElseThrow();
        return repo.findByUserId(user.getId());
    }
}
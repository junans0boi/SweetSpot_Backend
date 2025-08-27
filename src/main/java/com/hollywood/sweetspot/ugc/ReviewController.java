package com.hollywood.sweetspot.ugc;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hollywood.sweetspot.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewRepository repo;
    private final UserRepository users;
    private final ObjectMapper om = new ObjectMapper();

    record ReviewReq(
            @NotBlank String provider,
            @NotBlank String externalId,
            @Min(1) @Max(5) int rating,
            String content,
            List<String> photos
    ) {}

    @PostMapping
    public ResponseEntity<?> add(org.springframework.security.core.Authentication auth,
                                 @Valid @RequestBody ReviewReq req) throws Exception {
        var email = auth.getName();
        var user = users.findByEmail(email).orElseThrow();
        var saved = repo.save(Review.builder()
                .userId(user.getId())
                .provider(req.provider())
                .externalId(req.externalId())
                .rating(req.rating())
                .content(req.content())
                .photosJson((req.photos()==null || req.photos().isEmpty()) ? null : om.writeValueAsString(req.photos()))
                .build());
        return ResponseEntity.ok(Map.of("id", saved.getId()));
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam String provider, @RequestParam String externalId) {
        var list = repo.findByProviderAndExternalIdOrderByCreatedAtDesc(provider, externalId);
        var avg = repo.avgRating(provider, externalId);
        var cnt = repo.countByProviderAndExternalId(provider, externalId);
        return ResponseEntity.ok(Map.of(
                "stats", new ReviewStats(avg, cnt),
                "items", list
        ));
    }
}
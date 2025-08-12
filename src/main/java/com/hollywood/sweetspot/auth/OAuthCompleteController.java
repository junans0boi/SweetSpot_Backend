// src/main/java/com/hollywood/sweetspot/auth/OAuthCompleteController.java
package com.hollywood.sweetspot.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hollywood.sweetspot.security.TicketStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class OAuthCompleteController {
    private final TicketStore tickets;
    public OAuthCompleteController(TicketStore t){ this.tickets = t; }

    public record TokenPair(
        @JsonProperty("accessToken") String accessToken,
        @JsonProperty("refreshToken") String refreshToken,
        String type
    ) {}

    // 모바일 딥링크 이후: /api/auth/complete?ticket=...
    @GetMapping("/complete")
    public ResponseEntity<?> complete(@RequestParam String ticket) {
        var pair = tickets.take(ticket);
        if (pair == null) {
            return ResponseEntity.badRequest().body(Map.of("error","INVALID_TICKET"));
        }
        return ResponseEntity.ok(new TokenPair(pair[0], pair[1], "Bearer"));
    }
}
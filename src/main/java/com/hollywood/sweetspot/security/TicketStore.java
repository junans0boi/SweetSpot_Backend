// src/main/java/com/hollywood/sweetspot/security/TicketStore.java
package com.hollywood.sweetspot.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TicketStore {
    private static class Entry {
        final String access;
        final String refresh;
        final Instant expiresAt;
        Entry(String a, String r, Instant e) { access=a; refresh=r; expiresAt=e; }
    }
    private final Map<String, Entry> map = new ConcurrentHashMap<>();

    public String put(String access, String refresh, int ttlSeconds) {
        String ticket = UUID.randomUUID().toString();
        map.put(ticket, new Entry(access, refresh, Instant.now().plusSeconds(ttlSeconds)));
        return ticket;
    }

    public String[] take(String ticket) {
        var e = map.remove(ticket);
        if (e == null) return null;
        if (e.expiresAt.isBefore(Instant.now())) return null;
        return new String[]{ e.access, e.refresh };
    }
}
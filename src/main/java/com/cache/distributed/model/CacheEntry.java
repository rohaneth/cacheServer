package com.cache.distributed.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheEntry {
    private String key;
    private String value;
    private Instant createdAt;
    private Instant expiresAt;
    private long ttlSeconds;

    public CacheEntry(String key, String value, long ttlSeconds) {
        this.key = key;
        this.value = value;
        this.ttlSeconds = ttlSeconds;
        this.createdAt = Instant.now();
        this.expiresAt = Instant.now().plusSeconds(ttlSeconds);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}

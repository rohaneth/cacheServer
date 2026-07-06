package com.cache.distributed.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class CacheEntry {
    private String key;
    private String value;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Long ttlSeconds;

    public CacheEntry() {
    }

    public CacheEntry(String key, String value, LocalDateTime createdAt, LocalDateTime expiresAt, Long ttlSeconds) {
        this.key = key;
        this.value = value;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.ttlSeconds = ttlSeconds;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(Long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheEntry cacheEntry = (CacheEntry) o;
        return Objects.equals(key, cacheEntry.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "CacheEntry{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", ttlSeconds=" + ttlSeconds +
                '}';
    }
}

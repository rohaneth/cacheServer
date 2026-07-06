package com.cache.distributed.model;

import java.util.Objects;

public class CacheRequest {
    private String key;
    private String value;
    private Long ttlSeconds;

    public CacheRequest() {
    }

    public CacheRequest(String key, String value, Long ttlSeconds) {
        this.key = key;
        this.value = value;
        this.ttlSeconds = ttlSeconds;
    }

    public CacheRequest(String key) {
        this.key = key;
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

    public Long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(Long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheRequest that = (CacheRequest) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "CacheRequest{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", ttlSeconds=" + ttlSeconds +
                '}';
    }
}

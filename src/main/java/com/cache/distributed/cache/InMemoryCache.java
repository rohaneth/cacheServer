package com.cache.distributed.cache;

import com.cache.distributed.model.CacheEntry;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InMemoryCache {

    private final int maxSize;
    private final long defaultTtlSeconds;
    private final Map<String, CacheEntry> storage;
    private final ReadWriteLock lock;

    public InMemoryCache(int maxSize, long defaultTtlSeconds) {
        this.maxSize = maxSize;
        this.defaultTtlSeconds = defaultTtlSeconds;
        this.storage = new LinkedHashMap<String, CacheEntry>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
                return size() > InMemoryCache.this.maxSize;
            }
        };
        this.lock = new ReentrantReadWriteLock();
    }

    public CacheOperation put(String key, String value, Long ttlSeconds) {
        lock.writeLock().lock();
        try {
            long ttl = ttlSeconds != null ? ttlSeconds : defaultTtlSeconds;
            LocalDateTime now = LocalDateTime.now();

            CacheEntry entry = new CacheEntry(
                key,
                value,
                now,
                ttl > 0 ? now.plusSeconds(ttl) : null,
                ttl > 0 ? ttl : null
            );

            storage.put(key, entry);
            cleanupExpiredEntries();

            return CacheOperation.success(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public CacheOperation get(String key) {
        lock.readLock().lock();
        try {
            CacheEntry entry = storage.get(key);

            if (entry == null || entry.isExpired()) {
                return CacheOperation.failure(key, "Key not found or expired");
            }

            return CacheOperation.success(key, entry.getValue());
        } finally {
            lock.readLock().unlock();
        }
    }

    public CacheOperation delete(String key) {
        lock.writeLock().lock();
        try {
            CacheEntry removed = storage.remove(key);

            if (removed == null) {
                return CacheOperation.failure(key, "Key not found");
            }

            return CacheOperation.success(key, removed.getValue());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean exists(String key) {
        lock.readLock().lock();
        try {
            CacheEntry entry = storage.get(key);
            return entry != null && !entry.isExpired();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            storage.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int size() {
        lock.writeLock().lock();
        try {
            cleanupExpiredEntries();
            return storage.size();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void cleanupExpiredEntries() {
        storage.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}

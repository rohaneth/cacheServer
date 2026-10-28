package com.cache.distributed.cache;

import com.cache.distributed.model.CacheEntry;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class InMemoryCache {
    private final int maxSize;
    private final LinkedHashMap<String, CacheEntry> cache;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public InMemoryCache(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new LinkedHashMap<String, CacheEntry>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
                return size() > InMemoryCache.this.maxSize;
            }
        };
    }

    public CacheOperation put(String key, String value, int ttlSeconds) {
        lock.writeLock().lock();
        try {
            CacheEntry entry = new CacheEntry(key, value, ttlSeconds);
            cache.put(key, entry);
            log.debug("Stored key: {} with TTL: {} seconds", key, ttlSeconds);
            return CacheOperation.SUCCESS;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public CacheEntry get(String key) {
        lock.readLock().lock();
        try {
            CacheEntry entry = cache.get(key);
            if (entry == null) {
                return null;
            }
            if (entry.isExpired()) {
                lock.readLock().unlock();
                lock.writeLock().lock();
                try {
                    cache.remove(key);
                    log.debug("Removed expired key: {}", key);
                    return null;
                } finally {
                    lock.writeLock().unlock();
                    lock.readLock().lock();
                }
            }
            return entry;
        } finally {
            lock.readLock().unlock();
        }
    }

    public CacheOperation delete(String key) {
        lock.writeLock().lock();
        try {
            CacheEntry removed = cache.remove(key);
            if (removed == null) {
                return CacheOperation.KEY_NOT_FOUND;
            }
            log.debug("Deleted key: {}", key);
            return CacheOperation.SUCCESS;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return cache.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}

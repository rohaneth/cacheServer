package com.cache.distributed.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCacheTest {

    private InMemoryCache cache;

    @BeforeEach
    void setUp() {
        cache = new InMemoryCache(100, 10);
    }

    @Test
    void shouldStoreAndRetrieveValue() {
        cache.put("key1", "value1", null);

        CacheOperation result = cache.get("key1");

        assertTrue(result.isSuccess());
        assertEquals("value1", result.getValue());
    }

    @Test
    void shouldReturnNullForNonExistentKey() {
        CacheOperation result = cache.get("nonexistent");

        assertFalse(result.isSuccess());
        assertNull(result.getValue());
    }

    @Test
    void shouldDeleteExistingKey() {
        cache.put("key1", "value1", null);

        CacheOperation result = cache.delete("key1");

        assertTrue(result.isSuccess());
        assertFalse(cache.exists("key1"));
    }

    @Test
    void shouldReturnFailureWhenDeletingNonExistentKey() {
        CacheOperation result = cache.delete("nonexistent");

        assertFalse(result.isSuccess());
    }

    @Test
    void shouldEnforceMaxSizeLimit() {
        int maxSize = 5;
        InMemoryCache smallCache = new InMemoryCache(maxSize, 10);

        // Fill the cache
        for (int i = 0; i < maxSize; i++) {
            smallCache.put("key" + i, "value" + i, null);
        }

        assertEquals(maxSize, smallCache.size());

        // Add one more - should evict oldest
        smallCache.put("key" + maxSize, "value" + maxSize, null);

        assertEquals(maxSize, smallCache.size());
        assertFalse(smallCache.exists("key0"));
    }

    @Test
    void shouldExpireEntriesAfterTtl() throws InterruptedException {
        cache.put("ttl-key", "value", 1L); // 1 second TTL

        assertTrue(cache.exists("ttl-key"));

        Thread.sleep(1500); // Wait for expiration

        assertFalse(cache.exists("ttl-key"));
    }

    @Test
    void shouldClearAllEntries() {
        cache.put("key1", "value1", null);
        cache.put("key2", "value2", null);

        cache.clear();

        assertEquals(0, cache.size());
    }

    @Test
    void shouldHandleConcurrentOperations() throws InterruptedException {
        int threadCount = 10;
        int operationsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger operations = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        try {
                            String key = "thread-" + threadId + "-key-" + j;
                            cache.put(key, "value-" + j, null);
                            operations.incrementAndGet();
                            // Small delay to increase chance of concurrent access
                            if (j % 10 == 0) {
                                Thread.sleep(1);
                            }
                        } catch (Exception e) {
                            errors.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "Timeout waiting for threads");
        executor.shutdown();

        assertEquals(0, errors.get(), "Concurrent operations should not have errors");
        assertEquals(threadCount * operationsPerThread, operations.get(), "All operations should complete");
        assertTrue(cache.size() > 0, "Cache should have entries");
    }

    @Test
    void shouldUpdateExistingKey() {
        cache.put("key1", "value1", null);
        cache.put("key1", "value2", null);

        CacheOperation result = cache.get("key1");

        assertTrue(result.isSuccess());
        assertEquals("value2", result.getValue());
    }

    @Test
    void shouldUseDefaultTtlWhenNotSpecified() throws InterruptedException {
        InMemoryCache shortTtlCache = new InMemoryCache(100, 1); // 1 second default TTL

        shortTtlCache.put("key", "value", null);

        Thread.sleep(1500);

        assertFalse(shortTtlCache.exists("key"));
    }
}

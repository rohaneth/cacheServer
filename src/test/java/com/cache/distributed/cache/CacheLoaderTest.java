package com.cache.distributed.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CacheLoaderTest {

    private InMemoryCache cache;
    private CacheLoader loader;

    @BeforeEach
    void setUp() {
        cache = new InMemoryCache(100, 10);
        loader = new CacheLoader(cache);
    }

    @Test
    void shouldLoadValueWhenNotInCache() {
        AtomicInteger loadCount = new AtomicInteger(0);

        String result = loader.loadOrCompute("new-key", () -> {
            loadCount.incrementAndGet();
            return "computed-value";
        });

        assertEquals("computed-value", result);
        assertEquals(1, loadCount.get());
        assertEquals("computed-value", cache.get("new-key").getValue());
    }

    @Test
    void shouldReturnCachedValueWithoutRecomputing() {
        cache.put("cached-key", "cached-value", null);

        AtomicInteger loadCount = new AtomicInteger(0);

        String result = loader.loadOrCompute("cached-key", () -> {
            loadCount.incrementAndGet();
            return "should-not-be-called";
        });

        assertEquals("cached-value", result);
        assertEquals(0, loadCount.get());
    }

    @Test
    void shouldHandleConcurrentRequestsForSameKey() throws InterruptedException {
        AtomicInteger loadCount = new AtomicInteger(0);
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        String[] results = new String[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    results[index] = loader.loadOrCompute("shared-key", () -> {
                        loadCount.incrementAndGet();
                        try {
                            Thread.sleep(100); // Simulate slow computation
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return "shared-value-" + loadCount.get();
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Release all threads at once
        assertTrue(endLatch.await(30, TimeUnit.SECONDS), "Timeout waiting for threads");
        executor.shutdown();

        // Only one thread should have computed the value
        assertEquals(1, loadCount.get(), "Only one computation should occur");

        // All threads should get the same result
        for (String result : results) {
            assertEquals("shared-value-1", result, "All threads should get the same computed value");
        }
    }

    @Test
    void shouldPropagateExceptionsFromLoader() {
        RuntimeException expectedException = new RuntimeException("Computation failed");

        assertThrows(RuntimeException.class, () -> {
            loader.loadOrCompute("error-key", () -> {
                throw expectedException;
            });
        });
    }

    @Test
    void shouldHandleMultipleKeysIndependently() {
        AtomicInteger loadCount1 = new AtomicInteger(0);
        AtomicInteger loadCount2 = new AtomicInteger(0);

        loader.loadOrCompute("key-1", () -> {
            loadCount1.incrementAndGet();
            return "value-1";
        });

        loader.loadOrCompute("key-2", () -> {
            loadCount2.incrementAndGet();
            return "value-2";
        });

        // Both should be computed
        assertEquals(1, loadCount1.get());
        assertEquals(1, loadCount2.get());

        // Second calls should use cache
        loader.loadOrCompute("key-1", () -> {
            loadCount1.incrementAndGet();
            return "should-not-happen";
        });

        loader.loadOrCompute("key-2", () -> {
            loadCount2.incrementAndGet();
            return "should-not-happen";
        });

        assertEquals(1, loadCount1.get(), "Should use cached value");
        assertEquals(1, loadCount2.get(), "Should use cached value");
    }

    @Test
    void shouldCleanUpAfterFailedComputation() throws InterruptedException, ExecutionException {
        AtomicInteger loadCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        // First thread will fail
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<String> future1 = executor.submit(() -> {
            try {
                return loader.loadOrCompute("fail-key", () -> {
                    throw new RuntimeException("Intentional failure");
                });
            } finally {
                latch.countDown();
            }
        });

        // Wait for first thread to fail
        latch.await(5, TimeUnit.SECONDS);

        // Second thread should be able to compute successfully
        Future<String> future2 = executor.submit(() ->
            loader.loadOrCompute("fail-key", () -> {
                loadCount.incrementAndGet();
                return "recovered-value";
            })
        );

        assertThrows(ExecutionException.class, () -> future1.get());
        assertEquals("recovered-value", future2.get());
        assertEquals(1, loadCount.get());

        executor.shutdown();
    }
}

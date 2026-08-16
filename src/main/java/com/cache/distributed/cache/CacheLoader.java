package com.cache.distributed.cache;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class CacheLoader {

    private final InMemoryCache cache;
    private final Map<String, CompletableFuture<String>> loadingOperations;

    public CacheLoader(InMemoryCache cache) {
        this.cache = cache;
        this.loadingOperations = new ConcurrentHashMap<>();
    }

    public String loadOrCompute(String key, Supplier<String> loader) {
        // First try to get from cache without locking
        CacheOperation cached = cache.get(key);
        if (cached.isSuccess()) {
            return cached.getValue();
        }

        // Key not in cache, check if someone is already loading it
        CompletableFuture<String> existingFuture = loadingOperations.get(key);
        if (existingFuture != null) {
            try {
                return existingFuture.get(); // Wait for existing load to complete
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Failed to wait for cache load", e);
            }
        }

        // No one is loading this key, we'll do it
        CompletableFuture<String> newFuture = new CompletableFuture<>();
        CompletableFuture<String> race = loadingOperations.putIfAbsent(key, newFuture);

        if (race != null) {
            // Someone else started loading before us
            try {
                return race.get();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Failed to wait for cache load", e);
            }
        }

        // We won the race - load the value
        try {
            String value = loader.get();
            cache.put(key, value, null);
            newFuture.complete(value);
            return value;
        } catch (RuntimeException e) {
            newFuture.completeExceptionally(e);
            loadingOperations.remove(key);
            throw e;
        } finally {
            loadingOperations.remove(key);
        }
    }
}

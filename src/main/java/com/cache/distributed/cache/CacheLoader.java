package com.cache.distributed.cache;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CacheLoader {
    private final ConcurrentHashMap<String, CompletableFuture<String>> inFlightRequests = new ConcurrentHashMap<>();

    public CompletableFuture<String> load(String key, DataLoader loader) {
        return inFlightRequests.computeIfAbsent(key, k -> {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> loader.load(k));
            future.whenComplete((result, error) -> inFlightRequests.remove(k));
            return future;
        });
    }

    public interface DataLoader {
        String load(String key);
    }
}

package com.cache.distributed.node;

import com.cache.distributed.cache.InMemoryCache;
import com.cache.distributed.model.CacheRequest;
import com.cache.distributed.model.CacheResponse;
import com.cache.distributed.model.HealthStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CacheNodeService {

    private final InMemoryCache cache;
    private final NodeConfig config;

    @Autowired
    public CacheNodeService(InMemoryCache cache, NodeConfig config) {
        this.cache = cache;
        this.config = config;
    }

    public CacheResponse put(CacheRequest request) {
        cache.put(request.getKey(), request.getValue(), request.getTtlSeconds());
        return CacheResponse.success(request.getKey(), request.getValue(), config.getId());
    }

    public CacheResponse get(String key) {
        var result = cache.get(key);
        if (result.isSuccess()) {
            return CacheResponse.success(key, result.getValue(), config.getId());
        }
        return CacheResponse.failure(result.getMessage());
    }

    public CacheResponse delete(String key) {
        var result = cache.delete(key);
        if (result.isSuccess()) {
            return CacheResponse.success(key, null, config.getId());
        }
        return CacheResponse.failure(result.getMessage());
    }

    public HealthStatus getHealthStatus() {
        return HealthStatus.healthy(config.getId(), cache.size());
    }
}

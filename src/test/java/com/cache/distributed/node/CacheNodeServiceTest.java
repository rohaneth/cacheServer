package com.cache.distributed.node;

import com.cache.distributed.cache.InMemoryCache;
import com.cache.distributed.model.CacheRequest;
import com.cache.distributed.model.CacheResponse;
import com.cache.distributed.model.HealthStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CacheNodeServiceTest {

    private InMemoryCache cache;
    private NodeConfig config;
    private CacheNodeService service;

    @BeforeEach
    void setUp() {
        cache = new InMemoryCache(100, 10);
        config = new NodeConfig();
        config.setId("test-node");
        config.setMaxMemorySize(100);
        config.setTtlSeconds(3600);
        service = new CacheNodeService(cache, config);
    }

    @Test
    void shouldPutValueSuccessfully() {
        CacheRequest request = new CacheRequest("key1", "value1", null);

        CacheResponse response = service.put(request);

        assertTrue(response.isSuccess());
        assertEquals("key1", response.getKey());
        assertEquals("value1", response.getValue());
        assertEquals("test-node", response.getNodeId());
    }

    @Test
    void shouldGetValueSuccessfully() {
        cache.put("key1", "value1", null);

        CacheResponse response = service.get("key1");

        assertTrue(response.isSuccess());
        assertEquals("value1", response.getValue());
    }

    @Test
    void shouldReturnFailureWhenGettingNonExistentKey() {
        CacheResponse response = service.get("nonexistent");

        assertFalse(response.isSuccess());
    }

    @Test
    void shouldDeleteValueSuccessfully() {
        cache.put("key1", "value1", null);

        CacheResponse response = service.delete("key1");

        assertTrue(response.isSuccess());
        assertFalse(cache.exists("key1"));
    }

    @Test
    void shouldReturnFailureWhenDeletingNonExistentKey() {
        CacheResponse response = service.delete("nonexistent");

        assertFalse(response.isSuccess());
    }

    @Test
    void shouldReturnHealthStatus() {
        cache.put("key1", "value1", null);
        cache.put("key2", "value2", null);

        HealthStatus status = service.getHealthStatus();

        assertTrue(status.isHealthy());
        assertEquals("test-node", status.getNodeId());
        assertEquals(2, status.getCurrentSize());
    }
}

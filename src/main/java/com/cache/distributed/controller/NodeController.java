package com.cache.distributed.controller;

import com.cache.distributed.cache.InMemoryCache;
import com.cache.distributed.model.CacheEntry;
import com.cache.distributed.model.CacheRequest;
import com.cache.distributed.model.HealthStatus;
import com.cache.distributed.node.NodeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/node")
@Profile("node")
public class NodeController {
    @Autowired
    private InMemoryCache cache;

    @Autowired
    private NodeConfig nodeConfig;

    @PostMapping("/cache")
    public ResponseEntity<Void> put(@RequestBody CacheRequest request) {
        cache.put(request.getKey(), request.getValue(), (int) request.getTtlSeconds());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cache/{key}")
    public ResponseEntity<CacheEntry> get(@PathVariable String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(entry);
    }

    @DeleteMapping("/cache/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        cache.delete(key);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<HealthStatus> health() {
        HealthStatus status = new HealthStatus(nodeConfig.getNodeId(), cache.size());
        return ResponseEntity.ok(status);
    }
}

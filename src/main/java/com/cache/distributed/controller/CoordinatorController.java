package com.cache.distributed.controller;

import com.cache.distributed.cache.InMemoryCache;
import com.cache.distributed.health.NodeRegistry;
import com.cache.distributed.hashring.ConsistentHash;
import com.cache.distributed.model.CacheEntry;
import com.cache.distributed.model.CacheRequest;
import com.cache.distributed.model.CacheResponse;
import com.cache.distributed.model.NodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/coordinator")
@Profile("coordinator")
public class CoordinatorController {
    @Autowired
    private ConsistentHash consistentHash;

    @Autowired
    private NodeRegistry nodeRegistry;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/cache")
    public ResponseEntity<CacheResponse> put(@RequestBody CacheRequest request) {
        NodeInfo node = consistentHash.getNode(request.getKey());
        if (node == null) {
            return ResponseEntity.ok(CacheResponse.failure("No nodes available"));
        }

        try {
            String url = String.format("http://%s:%d/node/cache", node.getHost(), node.getPort());
            restTemplate.postForObject(url, request, Void.class);
            return ResponseEntity.ok(CacheResponse.success(null, node.getNodeId()));
        } catch (Exception e) {
            log.error("Failed to store key {} on node {}", request.getKey(), node.getNodeId(), e);
            return ResponseEntity.ok(CacheResponse.failure("Failed to store value"));
        }
    }

    @GetMapping("/cache/{key}")
    public ResponseEntity<CacheResponse> get(@PathVariable String key) {
        NodeInfo node = consistentHash.getNode(key);
        if (node == null) {
            return ResponseEntity.ok(CacheResponse.failure("No nodes available"));
        }

        try {
            String url = String.format("http://%s:%d/node/cache/%s", node.getHost(), node.getPort(), key);
            CacheEntry entry = restTemplate.getForObject(url, CacheEntry.class);
            if (entry == null) {
                return ResponseEntity.ok(CacheResponse.failure("Key not found"));
            }
            return ResponseEntity.ok(CacheResponse.success(entry.getValue(), node.getNodeId()));
        } catch (Exception e) {
            log.error("Failed to retrieve key {} from node {}", key, node.getNodeId(), e);
            return ResponseEntity.ok(CacheResponse.failure("Failed to retrieve value"));
        }
    }

    @DeleteMapping("/cache/{key}")
    public ResponseEntity<CacheResponse> delete(@PathVariable String key) {
        NodeInfo node = consistentHash.getNode(key);
        if (node == null) {
            return ResponseEntity.ok(CacheResponse.failure("No nodes available"));
        }

        try {
            String url = String.format("http://%s:%d/node/cache/%s", node.getHost(), node.getPort(), key);
            restTemplate.delete(url);
            return ResponseEntity.ok(CacheResponse.success(null, node.getNodeId()));
        } catch (Exception e) {
            log.error("Failed to delete key {} from node {}", key, node.getNodeId(), e);
            return ResponseEntity.ok(CacheResponse.failure("Failed to delete value"));
        }
    }

    @GetMapping("/nodes")
    public ResponseEntity<Collection<NodeInfo>> getNodes() {
        return ResponseEntity.ok(nodeRegistry.getAllNodes());
    }

    @PostMapping("/nodes/register")
    public ResponseEntity<Void> registerNode(@RequestBody NodeInfo nodeInfo) {
        nodeRegistry.register(nodeInfo);
        consistentHash.addNode(nodeInfo);
        log.info("Registered node: {}", nodeInfo.getNodeId());
        return ResponseEntity.ok().build();
    }
}

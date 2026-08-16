package com.cache.distributed.coordinator;

import com.cache.distributed.hashring.ConsistentHash;
import com.cache.distributed.model.CacheRequest;
import com.cache.distributed.model.CacheResponse;
import com.cache.distributed.model.NodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

@Service
public class CoordinatorService {

    private final ConsistentHash hashRing;
    private final RestTemplate restTemplate;

    @Autowired
    public CoordinatorService(ConsistentHash hashRing, RestTemplate restTemplate) {
        this.hashRing = hashRing;
        this.restTemplate = restTemplate;
    }

    public CacheResponse put(CacheRequest request) {
        try {
            NodeInfo targetNode = hashRing.getNode(request.getKey());
            String url = targetNode.getBaseUrl() + "/node/cache";

            CacheResponse response = restTemplate.postForObject(url, request, CacheResponse.class);
            return response != null ? response : CacheResponse.failure("No response from node");
        } catch (Exception e) {
            return CacheResponse.failure("Failed to store value: " + e.getMessage());
        }
    }

    public CacheResponse get(String key) {
        try {
            NodeInfo targetNode = hashRing.getNode(key);
            String url = targetNode.getBaseUrl() + "/node/cache/{key}";

            ResponseEntity<CacheResponse> response = restTemplate.getForEntity(url, CacheResponse.class, key);
            return response.getBody();
        } catch (Exception e) {
            return CacheResponse.failure("Failed to retrieve value: " + e.getMessage());
        }
    }

    public CacheResponse delete(String key) {
        try {
            NodeInfo targetNode = hashRing.getNode(key);
            String url = targetNode.getBaseUrl() + "/node/cache/{key}";

            restTemplate.delete(url, key);
            return CacheResponse.success(key, null, targetNode.getNodeId());
        } catch (Exception e) {
            return CacheResponse.failure("Failed to delete value: " + e.getMessage());
        }
    }

    public Set<NodeInfo> getAllNodes() {
        return hashRing.getNodes();
    }

    public void addNode(NodeInfo node) {
        hashRing.addNode(node);
    }

    public void removeNode(NodeInfo node) {
        hashRing.removeNode(node);
    }
}

package com.cache.distributed.integration;

import com.cache.distributed.coordinator.CoordinatorService;
import com.cache.distributed.hashring.ConsistentHash;
import com.cache.distributed.health.HealthMonitor;
import com.cache.distributed.health.NodeRegistry;
import com.cache.distributed.model.CacheRequest;
import com.cache.distributed.model.CacheResponse;
import com.cache.distributed.model.NodeInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DistributedCacheIntegrationTest {

    private ConsistentHash hashRing;
    private CoordinatorService coordinatorService;
    private NodeRegistry registry;
    private HealthMonitor healthMonitor;

    @BeforeEach
    void setUp() {
        hashRing = new ConsistentHash(100);
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        coordinatorService = new CoordinatorService(hashRing, restTemplate);
        registry = new NodeRegistry();
        healthMonitor = new HealthMonitor(hashRing, registry, 30);

        // Clear any existing nodes
        registry.getAllNodes().forEach(node -> registry.unregisterNode(node.getNodeId()));
    }

    @Test
    void shouldDistributeKeysAcrossNodes() {
        NodeInfo node1 = new NodeInfo("node-1", "localhost", 8081,
                                       NodeInfo.NodeStatus.ACTIVE, LocalDateTime.now(), 100);
        NodeInfo node2 = new NodeInfo("node-2", "localhost", 8082,
                                       NodeInfo.NodeStatus.ACTIVE, LocalDateTime.now(), 100);
        NodeInfo node3 = new NodeInfo("node-3", "localhost", 8083,
                                       NodeInfo.NodeStatus.ACTIVE, LocalDateTime.now(), 100);

        hashRing.addNode(node1);
        hashRing.addNode(node2);
        hashRing.addNode(node3);

        Set<String> assignedNodes = new java.util.HashSet<>();
        for (int i = 0; i < 100; i++) {
            NodeInfo node = hashRing.getNode("key-" + i);
            assignedNodes.add(node.getNodeId());
        }

        // All three nodes should receive some keys
        assertTrue(assignedNodes.contains("node-1"));
        assertTrue(assignedNodes.contains("node-2"));
        assertTrue(assignedNodes.contains("node-3"));
    }

    @Test
    void shouldHandleNodeFailure() {
        NodeInfo node1 = new NodeInfo("node-1", "localhost", 8081,
                                       NodeInfo.NodeStatus.ACTIVE, LocalDateTime.now(), 100);
        NodeInfo node2 = new NodeInfo("node-2", "localhost", 8082,
                                       NodeInfo.NodeStatus.ACTIVE, LocalDateTime.now(), 100);

        hashRing.addNode(node1);
        hashRing.addNode(node2);
        registry.registerNode(node1);
        registry.registerNode(node2);

        assertEquals(2, hashRing.getNodes().size());

        // Simulate node failure
        node1.setLastHeartbeat(LocalDateTime.now().minusSeconds(60));
        healthMonitor.checkNodeHealth();

        // Node1 should be removed
        assertEquals(1, hashRing.getNodes().size());
        assertFalse(hashRing.getNodes().contains(node1));
    }

    @Test
    void shouldMaintainConsistentRouting() {
        NodeInfo node1 = new NodeInfo("node-1", "localhost", 8081,
                                       NodeInfo.NodeStatus.ACTIVE, LocalDateTime.now(), 100);

        hashRing.addNode(node1);

        // Same key should always route to same node
        NodeInfo result1 = hashRing.getNode("consistent-key");
        NodeInfo result2 = hashRing.getNode("consistent-key");

        assertEquals(result1.getNodeId(), result2.getNodeId());
    }

    @Test
    void shouldHandleEmptyRing() {
        CacheRequest request = new CacheRequest("test-key", "test-value", null);

        CacheResponse response = coordinatorService.put(request);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("No nodes available"));
    }

    @Test
    void shouldAddAndRemoveNodesFromRegistry() {
        NodeInfo node = new NodeInfo("node-1", "localhost", 8081,
                                     NodeInfo.NodeStatus.ACTIVE, LocalDateTime.now(), 100);

        registry.registerNode(node);

        assertEquals(1, registry.getNodeCount());
        assertEquals(node, registry.getNode("node-1"));

        registry.unregisterNode("node-1");

        assertEquals(0, registry.getNodeCount());
        assertNull(registry.getNode("node-1"));
    }
}

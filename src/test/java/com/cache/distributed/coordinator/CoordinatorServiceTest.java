package com.cache.distributed.coordinator;

import com.cache.distributed.hashring.ConsistentHash;
import com.cache.distributed.model.CacheRequest;
import com.cache.distributed.model.CacheResponse;
import com.cache.distributed.model.NodeInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CoordinatorServiceTest {

    private ConsistentHash hashRing;
    private CoordinatorService coordinatorService;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        hashRing = new ConsistentHash(100);
        restTemplate = new RestTemplate();
        coordinatorService = new CoordinatorService(hashRing, restTemplate);
    }

    @Test
    void shouldThrowExceptionWhenNoNodesAvailable() {
        CacheRequest request = new CacheRequest("test-key", "test-value", null);

        CacheResponse response = coordinatorService.put(request);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("No nodes available"));
    }

    @Test
    void shouldAddAndRemoveNodes() {
        NodeInfo node = new NodeInfo("node-1", "localhost", 8081,
                                     NodeInfo.NodeStatus.ACTIVE, LocalDateTime.now(), 100);

        coordinatorService.addNode(node);

        Set<NodeInfo> nodes = coordinatorService.getAllNodes();
        assertTrue(nodes.contains(node));
        assertEquals(1, nodes.size());

        coordinatorService.removeNode(node);

        nodes = coordinatorService.getAllNodes();
        assertFalse(nodes.contains(node));
        assertEquals(0, nodes.size());
    }

    @Test
    void shouldReturnAllNodes() {
        NodeInfo node1 = new NodeInfo("node-1", "localhost", 8081,
                                      NodeInfo.NodeStatus.ACTIVE, LocalDateTime.now(), 100);
        NodeInfo node2 = new NodeInfo("node-2", "localhost", 8082,
                                      NodeInfo.NodeStatus.ACTIVE, LocalDateTime.now(), 100);

        coordinatorService.addNode(node1);
        coordinatorService.addNode(node2);

        Set<NodeInfo> nodes = coordinatorService.getAllNodes();
        assertEquals(2, nodes.size());
        assertTrue(nodes.stream().anyMatch(n -> n.getNodeId().equals("node-1")));
        assertTrue(nodes.stream().anyMatch(n -> n.getNodeId().equals("node-2")));
    }

    @Test
    void shouldFailToDeleteWhenNodeUnreachable() {
        NodeInfo node = new NodeInfo("node-1", "localhost", 9999,
                                     NodeInfo.NodeStatus.ACTIVE, LocalDateTime.now(), 100);
        coordinatorService.addNode(node);

        CacheResponse response = coordinatorService.delete("test-key");

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Failed to delete"));
    }

    @Test
    void shouldFailToGetWhenNodeUnreachable() {
        NodeInfo node = new NodeInfo("node-1", "localhost", 9999,
                                     NodeInfo.NodeStatus.ACTIVE, LocalDateTime.now(), 100);
        coordinatorService.addNode(node);

        CacheResponse response = coordinatorService.get("test-key");

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Failed to retrieve"));
    }
}

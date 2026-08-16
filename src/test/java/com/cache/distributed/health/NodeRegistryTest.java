package com.cache.distributed.health;

import com.cache.distributed.model.NodeInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class NodeRegistryTest {

    private NodeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new NodeRegistry();
    }

    @Test
    void shouldRegisterNode() {
        NodeInfo node = new NodeInfo("node-1", "localhost", 8081,
                                     NodeInfo.NodeStatus.ACTIVE, LocalDateTime.now(), 100);

        registry.registerNode(node);

        assertEquals(1, registry.getNodeCount());
        assertEquals(node, registry.getNode("node-1"));
    }

    @Test
    void shouldUnregisterNode() {
        NodeInfo node = new NodeInfo("node-1", "localhost", 8081,
                                     NodeInfo.NodeStatus.ACTIVE, LocalDateTime.now(), 100);

        registry.registerNode(node);
        registry.unregisterNode("node-1");

        assertEquals(0, registry.getNodeCount());
        assertNull(registry.getNode("node-1"));
    }

    @Test
    void shouldUpdateHeartbeat() {
        NodeInfo node = new NodeInfo("node-1", "localhost", 8081,
                                     NodeInfo.NodeStatus.ACTIVE,
                                     LocalDateTime.now().minusSeconds(60), 100);

        registry.registerNode(node);

        LocalDateTime oldHeartbeat = node.getLastHeartbeat();
        registry.updateHeartbeat("node-1");

        assertTrue(registry.getNode("node-1").getLastHeartbeat().isAfter(oldHeartbeat));
    }

    @Test
    void shouldReturnAllNodes() {
        NodeInfo node1 = new NodeInfo("node-1", "localhost", 8081,
                                      NodeInfo.NodeStatus.ACTIVE, LocalDateTime.now(), 100);
        NodeInfo node2 = new NodeInfo("node-2", "localhost", 8082,
                                      NodeInfo.NodeStatus.ACTIVE, LocalDateTime.now(), 100);

        registry.registerNode(node1);
        registry.registerNode(node2);

        Collection<NodeInfo> nodes = registry.getAllNodes();
        assertEquals(2, nodes.size());
        assertTrue(nodes.stream().anyMatch(n -> n.getNodeId().equals("node-1")));
        assertTrue(nodes.stream().anyMatch(n -> n.getNodeId().equals("node-2")));
    }

    @Test
    void shouldHandleMultipleRegistrationsOfSameNode() {
        NodeInfo node1 = new NodeInfo("node-1", "localhost", 8081,
                                      NodeInfo.NodeStatus.ACTIVE, LocalDateTime.now(), 100);
        NodeInfo node2 = new NodeInfo("node-1", "localhost", 8082,
                                      NodeInfo.NodeStatus.ACTIVE, LocalDateTime.now(), 100);

        registry.registerNode(node1);
        registry.registerNode(node2);

        assertEquals(1, registry.getNodeCount());
        // Should have the updated info (port 8082)
        assertEquals(8082, registry.getNode("node-1").getPort());
    }
}

package com.cache.distributed.hashring;

import com.cache.distributed.model.NodeInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConsistentHashTest {

    private ConsistentHash hashRing;
    private static final int VIRTUAL_NODES = 100;

    @BeforeEach
    void setUp() {
        hashRing = new ConsistentHash(VIRTUAL_NODES);
    }

    @Test
    void shouldAddNodeToRing() {
        NodeInfo node = new NodeInfo("node-1", "localhost", 8081,
                                      NodeInfo.NodeStatus.ACTIVE, null, VIRTUAL_NODES);
        hashRing.addNode(node);

        assertEquals(1, hashRing.getNodes().size());
        assertTrue(hashRing.getNodes().contains(node));
    }

    @Test
    void shouldRemoveNodeFromRing() {
        NodeInfo node = new NodeInfo("node-1", "localhost", 8081,
                                      NodeInfo.NodeStatus.ACTIVE, null, VIRTUAL_NODES);
        hashRing.addNode(node);
        hashRing.removeNode(node);

        assertEquals(0, hashRing.getNodes().size());
    }

    @Test
    void shouldRouteKeyToSpecificNode() {
        NodeInfo node1 = new NodeInfo("node-1", "localhost", 8081,
                                       NodeInfo.NodeStatus.ACTIVE, null, VIRTUAL_NODES);
        NodeInfo node2 = new NodeInfo("node-2", "localhost", 8082,
                                       NodeInfo.NodeStatus.ACTIVE, null, VIRTUAL_NODES);

        hashRing.addNode(node1);
        hashRing.addNode(node2);

        NodeInfo result = hashRing.getNode("my-key");

        assertNotNull(result);
        assertTrue(hashRing.getNodes().contains(result));
    }

    @Test
    void shouldRouteSameKeyToSameNode() {
        NodeInfo node1 = new NodeInfo("node-1", "localhost", 8081,
                                       NodeInfo.NodeStatus.ACTIVE, null, VIRTUAL_NODES);
        NodeInfo node2 = new NodeInfo("node-2", "localhost", 8082,
                                       NodeInfo.NodeStatus.ACTIVE, null, VIRTUAL_NODES);

        hashRing.addNode(node1);
        hashRing.addNode(node2);

        NodeInfo result1 = hashRing.getNode("consistent-key");
        NodeInfo result2 = hashRing.getNode("consistent-key");

        assertEquals(result1.getNodeId(), result2.getNodeId());
    }

    @Test
    void shouldDistributeKeysEvenlyAcrossNodes() {
        NodeInfo node1 = new NodeInfo("node-1", "localhost", 8081,
                                       NodeInfo.NodeStatus.ACTIVE, null, VIRTUAL_NODES);
        NodeInfo node2 = new NodeInfo("node-2", "localhost", 8082,
                                       NodeInfo.NodeStatus.ACTIVE, null, VIRTUAL_NODES);
        NodeInfo node3 = new NodeInfo("node-3", "localhost", 8083,
                                       NodeInfo.NodeStatus.ACTIVE, null, VIRTUAL_NODES);

        hashRing.addNode(node1);
        hashRing.addNode(node2);
        hashRing.addNode(node3);

        Set<String> assignedNodes = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            NodeInfo node = hashRing.getNode("key-" + i);
            assignedNodes.add(node.getNodeId());
        }

        // All three nodes should receive some keys
        assertEquals(3, assignedNodes.size());
    }

    @Test
    void shouldMinimizeKeyMigrationOnNodeRemoval() {
        NodeInfo node1 = new NodeInfo("node-1", "localhost", 8081,
                                       NodeInfo.NodeStatus.ACTIVE, null, VIRTUAL_NODES);
        NodeInfo node2 = new NodeInfo("node-2", "localhost", 8082,
                                       NodeInfo.NodeStatus.ACTIVE, null, VIRTUAL_NODES);
        NodeInfo node3 = new NodeInfo("node-3", "localhost", 8083,
                                       NodeInfo.NodeStatus.ACTIVE, null, VIRTUAL_NODES);

        hashRing.addNode(node1);
        hashRing.addNode(node2);
        hashRing.addNode(node3);

        // Record initial assignments
        java.util.Map<String, String> initialAssignments = new java.util.HashMap<>();
        for (int i = 0; i < 1000; i++) {
            String key = "key-" + i;
            initialAssignments.put(key, hashRing.getNode(key).getNodeId());
        }

        // Remove one node
        hashRing.removeNode(node2);

        // Count how many keys moved
        int movedKeys = 0;
        for (int i = 0; i < 1000; i++) {
            String key = "key-" + i;
            String oldNode = initialAssignments.get(key);
            String newNode = hashRing.getNode(key).getNodeId();

            // Keys that were on node2 should move, others should stay
            if (!oldNode.equals(newNode)) {
                movedKeys++;
            }
        }

        // Only keys from removed node should move (~1/3 of keys)
        // With 1000 keys and 3 nodes, expect ~333 keys to move
        assertTrue(movedKeys < 400, "Too many keys moved: " + movedKeys);
        assertTrue(movedKeys > 250, "Too few keys moved: " + movedKeys);
    }
}

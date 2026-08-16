package com.cache.distributed.hashring;

import com.cache.distributed.model.NodeInfo;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class ConsistentHash {

    private final int virtualNodesPerPhysical;
    private final SortedSet<VirtualNode> ring;
    private final Map<String, NodeInfo> physicalNodes;

    public ConsistentHash(int virtualNodesPerPhysical) {
        this.virtualNodesPerPhysical = virtualNodesPerPhysical;
        this.ring = new ConcurrentSkipListSet<>();
        this.physicalNodes = new HashMap<>();
    }

    public void addNode(NodeInfo node) {
        if (physicalNodes.containsKey(node.getNodeId())) {
            return; // Node already exists
        }

        physicalNodes.put(node.getNodeId(), node);

        // Create virtual nodes for this physical node
        for (int i = 0; i < virtualNodesPerPhysical; i++) {
            long hash = HashFunction.generateVirtualNodeHash(node.getNodeId(), i);
            ring.add(new VirtualNode(hash, node, i));
        }
    }

    public void removeNode(NodeInfo node) {
        if (!physicalNodes.containsKey(node.getNodeId())) {
            return; // Node doesn't exist
        }

        physicalNodes.remove(node.getNodeId());

        // Remove all virtual nodes for this physical node
        ring.removeIf(vn -> vn.getPhysicalNode().getNodeId().equals(node.getNodeId()));
    }

    public NodeInfo getNode(String key) {
        if (ring.isEmpty()) {
            throw new IllegalStateException("No nodes available in hash ring");
        }

        long keyHash = HashFunction.hash(key);

        // Find the first virtual node with hash >= key hash
        SortedSet<VirtualNode> tailSet = ring.tailSet(new VirtualNode(keyHash, null, 0));

        VirtualNode targetVirtualNode;
        if (tailSet.isEmpty()) {
            // Wrap around to the first node in the ring
            targetVirtualNode = ring.first();
        } else {
            targetVirtualNode = tailSet.first();
        }

        return targetVirtualNode.getPhysicalNode();
    }

    public Set<NodeInfo> getNodes() {
        return new HashSet<>(physicalNodes.values());
    }

    public int getVirtualNodeCount() {
        return ring.size();
    }
}

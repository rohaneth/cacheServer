package com.cache.distributed.hashring;

import com.cache.distributed.model.NodeInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Slf4j
public class ConsistentHash {
    private final Set<VirtualNode> ring = new ConcurrentSkipListSet<>();
    private final int virtualNodesPerPhysical;

    public ConsistentHash(int virtualNodesPerPhysical) {
        this.virtualNodesPerPhysical = virtualNodesPerPhysical;
    }

    public void addNode(NodeInfo node) {
        for (int i = 0; i < virtualNodesPerPhysical; i++) {
            long hash = HashFunction.hashVirtualNode(node.getNodeId(), i);
            VirtualNode virtualNode = new VirtualNode(hash, node, i);
            ring.add(virtualNode);
        }
        log.info("Added node {} with {} virtual nodes", node.getNodeId(), virtualNodesPerPhysical);
    }

    public void removeNode(NodeInfo node) {
        ring.removeIf(vn -> vn.getPhysicalNode().getNodeId().equals(node.getNodeId()));
        log.info("Removed node {} and its virtual nodes", node.getNodeId());
    }

    public NodeInfo getNode(String key) {
        if (ring.isEmpty()) {
            return null;
        }

        long keyHash = HashFunction.hash(key);
        VirtualNode virtualNode = findVirtualNode(keyHash);
        return virtualNode != null ? virtualNode.getPhysicalNode() : null;
    }

    private VirtualNode findVirtualNode(long keyHash) {
        for (VirtualNode vn : ring) {
            if (Long.compareUnsigned(vn.getHash(), keyHash) >= 0) {
                return vn;
            }
        }
        return ring.isEmpty() ? null : ring.iterator().next();
    }

    public int size() {
        return ring.size();
    }
}

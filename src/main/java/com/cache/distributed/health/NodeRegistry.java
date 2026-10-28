package com.cache.distributed.health;

import com.cache.distributed.model.NodeInfo;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NodeRegistry {
    private final ConcurrentHashMap<String, NodeInfo> nodes = new ConcurrentHashMap<>();

    public void register(NodeInfo node) {
        nodes.put(node.getNodeId(), node);
    }

    public void unregister(String nodeId) {
        nodes.remove(nodeId);
    }

    public NodeInfo get(String nodeId) {
        return nodes.get(nodeId);
    }

    public Collection<NodeInfo> getAllNodes() {
        return nodes.values();
    }

    public void updateHeartbeat(String nodeId) {
        NodeInfo node = nodes.get(nodeId);
        if (node != null) {
            node.setLastHeartbeat(Instant.now());
        }
    }

    public int size() {
        return nodes.size();
    }
}

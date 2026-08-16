package com.cache.distributed.health;

import com.cache.distributed.model.NodeInfo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NodeRegistry {

    private final Map<String, NodeInfo> registeredNodes;

    public NodeRegistry() {
        this.registeredNodes = new ConcurrentHashMap<>();
    }

    public void registerNode(NodeInfo node) {
        node.setLastHeartbeat(LocalDateTime.now());
        registeredNodes.put(node.getNodeId(), node);
    }

    public void unregisterNode(String nodeId) {
        registeredNodes.remove(nodeId);
    }

    public NodeInfo getNode(String nodeId) {
        return registeredNodes.get(nodeId);
    }

    public Collection<NodeInfo> getAllNodes() {
        return registeredNodes.values();
    }

    public void updateHeartbeat(String nodeId) {
        NodeInfo node = registeredNodes.get(nodeId);
        if (node != null) {
            node.setLastHeartbeat(LocalDateTime.now());
        }
    }

    public int getNodeCount() {
        return registeredNodes.size();
    }
}

package com.cache.distributed.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeInfo {
    private String nodeId;
    private String host;
    private int port;
    private NodeStatus status;
    private Instant lastHeartbeat;
    private int virtualNodeCount;

    public NodeInfo(String nodeId, String host, int port) {
        this.nodeId = nodeId;
        this.host = host;
        this.port = port;
        this.status = NodeStatus.ACTIVE;
        this.lastHeartbeat = Instant.now();
        this.virtualNodeCount = 100;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeInfo nodeInfo = (NodeInfo) o;
        return nodeId.equals(nodeInfo.nodeId);
    }

    @Override
    public int hashCode() {
        return nodeId.hashCode();
    }
}

package com.cache.distributed.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class NodeInfo {
    private String nodeId;
    private String host;
    private int port;
    private NodeStatus status;
    private LocalDateTime lastHeartbeat;
    private int virtualNodeCount;

    public NodeInfo() {
    }

    public NodeInfo(String nodeId, String host, int port, NodeStatus status, LocalDateTime lastHeartbeat, int virtualNodeCount) {
        this.nodeId = nodeId;
        this.host = host;
        this.port = port;
        this.status = status;
        this.lastHeartbeat = lastHeartbeat;
        this.virtualNodeCount = virtualNodeCount;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public int getVirtualNodeCount() {
        return virtualNodeCount;
    }

    public void setVirtualNodeCount(int virtualNodeCount) {
        this.virtualNodeCount = virtualNodeCount;
    }

    public String getBaseUrl() {
        return String.format("http://%s:%d", host, port);
    }

    public boolean isHealthy() {
        return status == NodeStatus.ACTIVE &&
               lastHeartbeat != null &&
               lastHeartbeat.isAfter(LocalDateTime.now().minusSeconds(30));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeInfo nodeInfo = (NodeInfo) o;
        return Objects.equals(nodeId, nodeInfo.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "nodeId='" + nodeId + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", status=" + status +
                ", lastHeartbeat=" + lastHeartbeat +
                ", virtualNodeCount=" + virtualNodeCount +
                '}';
    }

    public enum NodeStatus {
        ACTIVE, SUSPECTED, FAILED
    }
}

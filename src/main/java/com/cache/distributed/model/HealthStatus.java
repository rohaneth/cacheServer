package com.cache.distributed.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class HealthStatus {
    private String nodeId;
    private boolean healthy;
    private int currentSize;
    private LocalDateTime timestamp;

    public HealthStatus() {
    }

    public HealthStatus(String nodeId, boolean healthy, int currentSize, LocalDateTime timestamp) {
        this.nodeId = nodeId;
        this.healthy = healthy;
        this.currentSize = currentSize;
        this.timestamp = timestamp;
    }

    public static HealthStatus healthy(String nodeId, int size) {
        return new HealthStatus(nodeId, true, size, LocalDateTime.now());
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(int currentSize) {
        this.currentSize = currentSize;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthStatus that = (HealthStatus) o;
        return healthy == that.healthy && currentSize == that.currentSize && Objects.equals(nodeId, that.nodeId) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, healthy, currentSize, timestamp);
    }

    @Override
    public String toString() {
        return "HealthStatus{" +
                "nodeId='" + nodeId + '\'' +
                ", healthy=" + healthy +
                ", currentSize=" + currentSize +
                ", timestamp=" + timestamp +
                '}';
    }
}

package com.cache.distributed.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthStatus {
    private String nodeId;
    private int currentSize;
    private Instant timestamp;
    private String status;

    public HealthStatus(String nodeId, int currentSize) {
        this.nodeId = nodeId;
        this.currentSize = currentSize;
        this.timestamp = Instant.now();
        this.status = "HEALTHY";
    }
}

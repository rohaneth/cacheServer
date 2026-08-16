package com.cache.distributed.coordinator;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "coordinator")
public class CoordinatorConfig {
    private int virtualNodes = 100;
    private int healthCheckIntervalSeconds = 10;
    private int nodeTimeoutSeconds = 30;

    public int getVirtualNodes() {
        return virtualNodes;
    }

    public void setVirtualNodes(int virtualNodes) {
        this.virtualNodes = virtualNodes;
    }

    public int getHealthCheckIntervalSeconds() {
        return healthCheckIntervalSeconds;
    }

    public void setHealthCheckIntervalSeconds(int healthCheckIntervalSeconds) {
        this.healthCheckIntervalSeconds = healthCheckIntervalSeconds;
    }

    public int getNodeTimeoutSeconds() {
        return nodeTimeoutSeconds;
    }

    public void setNodeTimeoutSeconds(int nodeTimeoutSeconds) {
        this.nodeTimeoutSeconds = nodeTimeoutSeconds;
    }
}

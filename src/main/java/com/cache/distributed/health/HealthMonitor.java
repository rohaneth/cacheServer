package com.cache.distributed.health;

import com.cache.distributed.hashring.ConsistentHash;
import com.cache.distributed.model.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;

@Component
public class HealthMonitor {

    private static final Logger log = LoggerFactory.getLogger(HealthMonitor.class);

    private final ConsistentHash hashRing;
    private final NodeRegistry registry;
    private final long timeoutSeconds;

    @Autowired
    public HealthMonitor(ConsistentHash hashRing, NodeRegistry registry,
                         @Value("${coordinator.node-timeout-seconds:30}") long timeoutSeconds) {
        this.hashRing = hashRing;
        this.registry = registry;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Scheduled(fixedRateString = "${coordinator.health-check-interval-seconds:10}000")
    public void checkNodeHealth() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(timeoutSeconds);

        // Check all registered nodes
        Collection<NodeInfo> nodes = registry.getAllNodes();
        for (NodeInfo node : nodes) {
            if (node.getLastHeartbeat().isBefore(cutoff)) {
                log.warn("Node {} is unhealthy, removing from ring", node.getNodeId());

                // Remove from hash ring
                hashRing.removeNode(node);

                // Unregister from registry
                registry.unregisterNode(node.getNodeId());
            } else if (!hashRing.getNodes().contains(node)) {
                // Node is healthy but not in ring (reconnected or new)
                log.info("Adding healthy node {} to ring", node.getNodeId());
                hashRing.addNode(node);
            }
        }
    }
}

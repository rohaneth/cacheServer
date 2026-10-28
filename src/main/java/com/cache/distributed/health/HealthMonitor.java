package com.cache.distributed.health;

import com.cache.distributed.hashring.ConsistentHash;
import com.cache.distributed.model.NodeInfo;
import com.cache.distributed.model.NodeStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;

@Slf4j
@Component
@Profile("coordinator")
public class HealthMonitor {
    @Autowired
    private NodeRegistry nodeRegistry;

    @Autowired
    private ConsistentHash consistentHash;

    @Value("${coordinator.node-timeout-seconds:15}")
    private long nodeTimeoutSeconds;

    @Scheduled(fixedRateString = "${coordinator.health-check-interval-seconds:5}000")
    public void monitorNodeHealth() {
        Collection<NodeInfo> nodes = nodeRegistry.getAllNodes();
        Instant now = Instant.now();

        for (NodeInfo node : nodes) {
            long secondsSinceHeartbeat = now.getEpochSecond() - node.getLastHeartbeat().getEpochSecond();

            if (secondsSinceHeartbeat > nodeTimeoutSeconds) {
                if (node.getStatus() == NodeStatus.ACTIVE) {
                    node.setStatus(NodeStatus.SUSPECTED);
                    log.warn("Node {} suspected - no heartbeat for {} seconds", node.getNodeId(), secondsSinceHeartbeat);
                } else if (node.getStatus() == NodeStatus.SUSPECTED && secondsSinceHeartbeat > nodeTimeoutSeconds * 2) {
                    node.setStatus(NodeStatus.FAILED);
                    consistentHash.removeNode(node);
                    log.error("Node {} marked as FAILED and removed from hash ring", node.getNodeId());
                }
            } else if (node.getStatus() != NodeStatus.ACTIVE) {
                node.setStatus(NodeStatus.ACTIVE);
                consistentHash.addNode(node);
                log.info("Node {} recovered and re-added to hash ring", node.getNodeId());
            }
        }
    }
}

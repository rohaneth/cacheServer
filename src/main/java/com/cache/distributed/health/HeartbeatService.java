package com.cache.distributed.health;

import com.cache.distributed.model.HealthStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;

@Service
public class HeartbeatService {

    private static final Logger log = LoggerFactory.getLogger(HeartbeatService.class);

    private final NodeRegistry registry;
    private final RestTemplate restTemplate;

    @Autowired
    public HeartbeatService(NodeRegistry registry, RestTemplate restTemplate) {
        this.registry = registry;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRateString = "${coordinator.health-check-interval-seconds:10}000")
    public void sendHeartbeats() {
        Collection<com.cache.distributed.model.NodeInfo> nodes = registry.getAllNodes();
        for (com.cache.distributed.model.NodeInfo node : nodes) {
            try {
                String url = node.getBaseUrl() + "/node/health";
                HealthStatus status = restTemplate.getForObject(url, HealthStatus.class);

                if (status != null && status.isHealthy()) {
                    registry.updateHeartbeat(node.getNodeId());
                }
            } catch (Exception e) {
                // Node is unreachable, will be marked for removal by HealthMonitor
                log.debug("Node {} unreachable during heartbeat check", node.getNodeId());
            }
        }
    }
}

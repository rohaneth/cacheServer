package com.cache.distributed.health;

import com.cache.distributed.model.HealthStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;

@Slf4j
@Component
@Profile("coordinator")
public class HeartbeatService {
    @Autowired
    private NodeRegistry nodeRegistry;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${coordinator.health-check-interval-seconds:5}")
    private int healthCheckInterval;

    @Scheduled(fixedRateString = "${coordinator.health-check-interval-seconds:5}000")
    public void checkNodeHealth() {
        Collection<com.cache.distributed.model.NodeInfo> nodes = nodeRegistry.getAllNodes();
        for (com.cache.distributed.model.NodeInfo node : nodes) {
            try {
                String url = String.format("http://%s:%d/node/health", node.getHost(), node.getPort());
                HealthStatus status = restTemplate.getForObject(url, HealthStatus.class);
                if (status != null) {
                    nodeRegistry.updateHeartbeat(node.getNodeId());
                    log.debug("Health check passed for node {}", node.getNodeId());
                }
            } catch (Exception e) {
                log.warn("Health check failed for node {}: {}", node.getNodeId(), e.getMessage());
            }
        }
    }
}

package com.cache.distributed.node;

import com.cache.distributed.model.NodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@Profile("node")
public class NodeRegistrationService {
    @Autowired
    private NodeConfig nodeConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${coordinator.url:http://localhost:8080}")
    private String coordinatorUrl;

    @EventListener(ApplicationReadyEvent.class)
    public void registerWithCoordinator() {
        try {
            NodeInfo nodeInfo = new NodeInfo(
                nodeConfig.getNodeId(),
                nodeConfig.getHost(),
                nodeConfig.getPort()
            );

            String url = coordinatorUrl + "/coordinator/nodes/register";
            restTemplate.postForObject(url, nodeInfo, Void.class);
            log.info("Successfully registered node {} with coordinator at {}", nodeConfig.getNodeId(), coordinatorUrl);
        } catch (Exception e) {
            log.error("Failed to register with coordinator: {}", e.getMessage());
        }
    }
}

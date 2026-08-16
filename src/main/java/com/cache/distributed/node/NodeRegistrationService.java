package com.cache.distributed.node;

import com.cache.distributed.model.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@ConditionalOnProperty(name = "spring.config.activate.on-profile", havingValue = "node")
public class NodeRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(NodeRegistrationService.class);

    private final NodeConfig config;
    private final RestTemplate restTemplate;

    @Value("${server.port}")
    private int serverPort;

    @Autowired
    public NodeRegistrationService(NodeConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerWithCoordinator() {
        try {
            String coordinatorUrl = "http://localhost:8080/coordinator/nodes/register";
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setNodeId(config.getId());
            nodeInfo.setHost("localhost");
            nodeInfo.setPort(serverPort);
            nodeInfo.setStatus(NodeInfo.NodeStatus.ACTIVE);
            nodeInfo.setLastHeartbeat(java.time.LocalDateTime.now());
            nodeInfo.setVirtualNodeCount(100);

            restTemplate.postForObject(coordinatorUrl, nodeInfo, String.class);
            log.info("Successfully registered node {} with coordinator", config.getId());
        } catch (Exception e) {
            log.warn("Failed to register with coordinator: {}", e.getMessage());
        }
    }
}

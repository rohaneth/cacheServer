package com.cache.distributed.node;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Data
@Configuration
@Profile("node")
@ConfigurationProperties(prefix = "node")
public class NodeConfig {
    private String nodeId;
    private String host = "localhost";
    private int port = 8081;
    private int maxMemory = 1000;
    private int defaultTtl = 3600;
}

package com.cache.distributed.config;

import com.cache.distributed.cache.InMemoryCache;
import com.cache.distributed.hashring.ConsistentHash;
import com.cache.distributed.node.NodeConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("coordinator")
public class CoordinatorConfig {
    @Bean
    public ConsistentHash consistentHash(@Value("${coordinator.virtual-nodes-per-physical:100}") int virtualNodesPerPhysical) {
        return new ConsistentHash(virtualNodesPerPhysical);
    }
}

@Configuration
@Profile("node")
class CacheConfig {
    @Bean
    public InMemoryCache inMemoryCache(NodeConfig nodeConfig) {
        return new InMemoryCache(nodeConfig.getMaxMemory());
    }
}

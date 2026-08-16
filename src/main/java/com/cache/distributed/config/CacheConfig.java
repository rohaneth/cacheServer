package com.cache.distributed.config;

import com.cache.distributed.cache.CacheLoader;
import com.cache.distributed.cache.InMemoryCache;
import com.cache.distributed.coordinator.CoordinatorConfig;
import com.cache.distributed.hashring.ConsistentHash;
import com.cache.distributed.node.NodeConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    @ConditionalOnBean(NodeConfig.class)
    public InMemoryCache inMemoryCache(NodeConfig nodeConfig) {
        return new InMemoryCache(nodeConfig.getMaxMemorySize(), nodeConfig.getTtlSeconds());
    }

    @Bean
    @ConditionalOnBean(InMemoryCache.class)
    public CacheLoader cacheLoader(InMemoryCache cache) {
        return new CacheLoader(cache);
    }

    @Bean
    @ConditionalOnBean(CoordinatorConfig.class)
    public ConsistentHash consistentHash(CoordinatorConfig coordinatorConfig) {
        return new ConsistentHash(coordinatorConfig.getVirtualNodes());
    }
}

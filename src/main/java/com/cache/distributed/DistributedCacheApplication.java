package com.cache.distributed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DistributedCacheApplication {
    public static void main(String[] args) {
        SpringApplication.run(DistributedCacheApplication.class, args);
    }
}

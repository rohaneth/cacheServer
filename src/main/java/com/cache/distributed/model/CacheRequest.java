package com.cache.distributed.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheRequest {
    private String key;
    private String value;
    private long ttlSeconds;
}

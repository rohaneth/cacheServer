package com.cache.distributed.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheResponse {
    private boolean success;
    private String value;
    private String message;
    private String nodeId;

    public static CacheResponse success(String value, String nodeId) {
        return new CacheResponse(true, value, "Operation successful", nodeId);
    }

    public static CacheResponse failure(String message) {
        return new CacheResponse(false, null, message, null);
    }
}

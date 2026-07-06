package com.cache.distributed.model;

import java.util.Objects;

public class CacheResponse {
    private boolean success;
    private String key;
    private String value;
    private String message;
    private String nodeId;

    public CacheResponse() {
    }

    public CacheResponse(boolean success, String key, String value, String message, String nodeId) {
        this.success = success;
        this.key = key;
        this.value = value;
        this.message = message;
        this.nodeId = nodeId;
    }

    public static CacheResponse success(String key, String value, String nodeId) {
        return new CacheResponse(true, key, value, "Operation successful", nodeId);
    }

    public static CacheResponse failure(String message) {
        return new CacheResponse(false, null, null, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheResponse that = (CacheResponse) o;
        return success == that.success && Objects.equals(key, that.key) && Objects.equals(value, that.value) && Objects.equals(message, that.message) && Objects.equals(nodeId, that.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, key, value, message, nodeId);
    }

    @Override
    public String toString() {
        return "CacheResponse{" +
                "success=" + success +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", message='" + message + '\'' +
                ", nodeId='" + nodeId + '\'' +
                '}';
    }
}

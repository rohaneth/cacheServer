package com.cache.distributed.cache;

public class CacheOperation {
    private boolean success;
    private String key;
    private String value;
    private String message;

    public CacheOperation() {
    }

    public CacheOperation(boolean success, String key, String value, String message) {
        this.success = success;
        this.key = key;
        this.value = value;
        this.message = message;
    }

    public static CacheOperation success(String key, String value) {
        return new CacheOperation(true, key, value, "Operation successful");
    }

    public static CacheOperation failure(String key, String message) {
        return new CacheOperation(false, key, null, message);
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
}

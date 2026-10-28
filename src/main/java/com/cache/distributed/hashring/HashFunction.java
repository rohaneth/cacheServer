package com.cache.distributed.hashring;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashFunction {
    private static final String ALGORITHM = "MD5";

    public static long hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            byte[] digest = md.digest(key.getBytes(StandardCharsets.UTF_8));
            return bytesToLong(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not found", e);
        }
    }

    public static long hashVirtualNode(String nodeId, int replicaIndex) {
        return hash(nodeId + "-" + replicaIndex);
    }

    private static long bytesToLong(byte[] bytes) {
        long value = 0;
        for (int i = 0; i < 4; i++) {
            value = (value << 8) | (bytes[i] & 0xFF);
        }
        return value;
    }
}

package com.cache.distributed.hashring;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashFunction {

    private static final int RING_SIZE = 2_147_483_647; // Integer.MAX_VALUE

    public static long hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(key.getBytes(StandardCharsets.UTF_8));

            // Use first 4 bytes of MD5 hash
            long hash = ((long) (digest[0] & 0xFF) << 24) |
                        ((long) (digest[1] & 0xFF) << 16) |
                        ((long) (digest[2] & 0xFF) << 8) |
                        ((long) (digest[3] & 0xFF));

            return hash & 0x00000000FFFFFFFFL; // Ensure positive
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not available", e);
        }
    }

    public static long generateVirtualNodeHash(String nodeId, int replicaIndex) {
        return hash(nodeId + "-" + replicaIndex);
    }
}

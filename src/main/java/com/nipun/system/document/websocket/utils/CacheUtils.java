package com.nipun.system.document.websocket.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class CacheUtils {
    private final RedisTemplate<String, Object> redisTemplate;

    public <T> String generateCacheKey(T documentId, T branchId) {
        return documentId + ":" + branchId;
    }

    public void refreshTTL(String cacheName, String key, int ttl) {
        String redisKey = generateCacheKey(cacheName, key);
        redisTemplate.expire(redisKey, ttl, TimeUnit.MINUTES);
    }
}

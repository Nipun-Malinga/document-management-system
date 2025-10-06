package com.nipun.system.user.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class UserCacheServiceImpl implements UserCacheService {

    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${cache.names.user.websocket.user-connection.users}")
    private String USERS;
    @Value("${cache.names.user.websocket.user-connection.sessions}")
    private String SESSIONS;
    @Value("${cache.names.user.websocket.cache-ttl}")
    private int CACHE_EXPIRE_TTL;

    @Override
    public Long getConnectedUserIdFromSession(String sessionId) {
        var cache = cacheManager.getCache(SESSIONS);

        if (cache == null) return null;

        var userId = cache.get(sessionId, String.class);

        if (userId == null) return null;

        refreshTTL(SESSIONS, sessionId);

        return objectMapper.convertValue(userId, new TypeReference<>() {
        });
    }

    @Override
    public void putConnectedSession(String sessionId, Long userId) {
        var cache = cacheManager.getCache(SESSIONS);

        if (cache == null) return;

        cache.put(sessionId, userId.toString());

        refreshTTL(SESSIONS, sessionId);
    }

    @Override
    public void removeConnectedSession(String sessionId) {
        var cache = cacheManager.getCache(SESSIONS);

        if (cache == null) return;

        cache.evict(sessionId);
    }

    @Override
    public Set<Long> getConnectedUsers() {
        var cache = cacheManager.getCache(USERS);

        if (cache == null) return null;

        var users = cache.get("users", Object.class);

        if (users == null) return null;

        refreshTTL(USERS, "users");

        return objectMapper.convertValue(users, new TypeReference<>() {
        });
    }

    @Override
    public void putConnectedUsers(Set<Long> users) {
        var cache = cacheManager.getCache(USERS);

        if (cache == null) return;

        cache.put("users", users);

        refreshTTL(USERS, "users");
    }


    private void refreshTTL(String cacheName, String key) {
        String redisKey = cacheName + "::" + key;
        redisTemplate.expire(redisKey, CACHE_EXPIRE_TTL, TimeUnit.MINUTES);
    }
}

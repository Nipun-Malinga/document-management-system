package com.nipun.system.user.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class UserCacheServiceImpl implements UserCacheService {

    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;
    @Value("${cache.names.user.websocket.user-connection.users}")
    private String USERS;
    @Value("${cache.names.user.websocket.user-connection.sessions}")
    private String SESSIONS;

    @Override
    public Long getConnectedUserIdFromSession(String sessionId) {
        var cache = cacheManager.getCache(SESSIONS);

        if (cache == null) return null;

        var userId = cache.get(sessionId, String.class);

        if (userId == null) return null;

        return objectMapper.convertValue(userId, new TypeReference<>() {
        });
    }

    @Override
    public void putConnectedSession(String sessionId, Long userId) {
        var cache = cacheManager.getCache(SESSIONS);

        if (cache == null) return;

        cache.put(sessionId, userId.toString());
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

        return objectMapper.convertValue(users, new TypeReference<>() {
        });
    }

    @Override
    public void putConnectedUsers(Set<Long> users) {
        var cache = cacheManager.getCache(USERS);

        if (cache == null) return;

        cache.put("users", users);
    }


}

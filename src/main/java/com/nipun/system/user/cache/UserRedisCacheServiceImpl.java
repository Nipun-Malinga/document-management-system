package com.nipun.system.user.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Set;

@AllArgsConstructor
@Service
public class UserRedisCacheServiceImpl implements UserCacheService{

    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    private static final String SESSIONS = "CONNECTED_SESSION_CACHE";
    private static final String USERS = "CONNECTED_USERS_CACHE";


    @Override
    public Long getConnectedUserIdFromSession(String sessionId) {
        var cache = cacheManager.getCache(SESSIONS);

        if (cache == null) return null;

        var userId = cache.get(sessionId, String.class);

        if (userId == null) return null;

        return objectMapper.convertValue(userId, new TypeReference<>() {});
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

        if (users == null)  return null;

        return objectMapper.convertValue(users, new TypeReference<>() {});
    }

    @Override
    public void putConnectedUsers(Set<Long> users) {
        var cache = cacheManager.getCache(USERS);

        if (cache == null) return;

        cache.put("users", users);
    }


}

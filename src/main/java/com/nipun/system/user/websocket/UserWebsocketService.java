package com.nipun.system.user.websocket;

import com.nipun.system.document.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class UserWebsocketService {
    private final CacheManager cacheManager;

    public Long geConnectedUserIdFromSession(String sessionId) {
        var sessionCache = cacheManager.getCache("CONNECTED_SESSION_CACHE");

        if(sessionCache == null) return null;

        if(sessionCache.get(sessionId) == null)  return null;

        return Long.valueOf(Objects.requireNonNull(sessionCache.get(sessionId, String.class)));
    }

    public void addConnectedSessionToCache(String sessionId, Principal principal) {
        var sessionCache = cacheManager.getCache("CONNECTED_SESSION_CACHE");

        var userId = Utils.getUserIdFromPrincipal(principal);

        if(sessionCache != null) {
            sessionCache.put(sessionId, userId.toString());
            addConnectedUsersToCache(userId);
        }
    }

    public Long removeDisconnectedSessionFromCache(String sessionId) {
        var sessionCache = cacheManager.getCache("CONNECTED_SESSION_CACHE");

        if(sessionCache == null) return null;

        if(sessionCache.get(sessionId) == null)  return null;

        var userId = Long.valueOf(Objects.requireNonNull(sessionCache.get(sessionId, String.class)));

        removeDisconnectedUserFromCache(userId);

        sessionCache.evict(sessionId);

        return userId;
    }

    public Boolean isUserOnline(Long userId) {
        var connectedUsersCache = cacheManager.getCache("CONNECTED_USERS_CACHE");

        if (connectedUsersCache != null && userId != null) {
            Set<Long> users = connectedUsersCache.get("users", HashSet.class);

            if(users != null) {
                return users.contains(userId);
            }
        }

        return false;
    }

    private void addConnectedUsersToCache(Long userId) {
        var connectedUsersCache = cacheManager.getCache("CONNECTED_USERS_CACHE");

        if (connectedUsersCache != null) {
            Set<Long> users = connectedUsersCache.get("users", HashSet.class);

            if (users == null) users = new HashSet<>();

            users.add(userId);
            connectedUsersCache.put("users", users);
        }
    }

    private void removeDisconnectedUserFromCache(Long userId) {
        var connectedUsersCache = cacheManager.getCache("CONNECTED_USERS_CACHE");

        if (connectedUsersCache != null) {
            Set<Long> users = connectedUsersCache.get("users", HashSet.class);

            if(users != null) {
                users.remove(userId);
                connectedUsersCache.put("users",  users);
            }
        }
    }
}

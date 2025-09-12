package com.nipun.system.document.cache;

import com.nipun.system.document.websocket.AuthorizedOptions;
import com.nipun.system.document.websocket.ConnectedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
public class DocumentRedisCacheServiceImpl implements DocumentCacheService {

    private final CacheManager cacheManager;

    private static final String USER_PERMISSION = "DOCUMENT_USER_PERMISSION_CACHE";
    private static final String STATUS = "DOCUMENT_STATUS_CACHE";
    private static final String SESSION = "DOCUMENT_SESSION_CACHE";
    private static final String CONNECTED_USERS = "DOCUMENT_CONNECTED_USERS_CACHE";

    @Override
    public Map<String, AuthorizedOptions> getDocumentUserPermissions(UUID documentId) {
        var cache = cacheManager.getCache(USER_PERMISSION);

        if (cache == null) return null;

        return cache.get(documentId, ConcurrentHashMap.class);
    }

    @Override
    public void putDocumentUserPermissions(UUID documentId, Map<String, AuthorizedOptions> permissions) {
        var cache = cacheManager.getCache(USER_PERMISSION);

        if (cache == null) return;

        cache.put(documentId, permissions);
    }

    @Override
    public String getDocumentCurrentStatus(UUID documentId) {
        var cache = cacheManager.getCache(STATUS);

        if (cache == null) return null;

        return cache.get(documentId, String.class);
    }

    @Override
    public void putDocumentCurrentStatus(UUID documentId, String status) {
        var cache = cacheManager.getCache(STATUS);

        if (cache == null) return;

        cache.put(documentId, status);
    }

    @Override
    public Set<Long> getDocumentConnectedSessions(UUID documentId) {
        var cache = cacheManager.getCache(SESSION);

        if (cache == null) return null;

        return cache.get(documentId, HashSet.class);
    }

    @Override
    public void putDocumentConnectedSession(UUID documentId, Set<Long> sessions) {
        var cache = cacheManager.getCache(SESSION);

        if (cache == null) return;

        cache.put(documentId, sessions);
    }

    @Override
    public void removeDocumentConnectedSession(String sessionId) {
        var cache = cacheManager.getCache(SESSION);

        if (cache == null) return;

        cache.evict(sessionId);
    }

    @Override
    public ConnectedUser getDocumentConnectedUsers(String sessionId) {
        var cache = cacheManager.getCache(CONNECTED_USERS);

        if (cache == null) return null;

        return cache.get(sessionId, ConnectedUser.class);
    }

    @Override
    public void putDocumentConnectedUsers(String sessionId, ConnectedUser connectedUser) {
        var cache = cacheManager.getCache(CONNECTED_USERS);

        if (cache == null) return;

        cache.put(sessionId, connectedUser);
    }
}

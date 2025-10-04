package com.nipun.system.document.websocket.connection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class ConnectionCacheServiceImpl implements ConnectionCacheService {

    private static final String DOCUMENT_BRANCH_USERS = "DOCUMENT_BRANCH_CONNECTED_USERS_CACHE";
    private static final String DOCUMENT_BRANCH_SESSIONS = "DOCUMENT_BRANCH_CONNECTED_SESSIONS_CACHE";

    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    @Override
    public void setConnectedUser(UUID documentId, UUID branchId, Long userId) {
        var cache = cacheManager.getCache(DOCUMENT_BRANCH_USERS);

        if (cache == null)
            return;

        var users = objectMapper.convertValue(
                cache.get(getCacheKey(documentId, branchId), Object.class),
                new TypeReference<Set<Long>>() {
                }
        );

        if (users == null)
            users = new HashSet<>();

        users.add(userId);

        cache.put(getCacheKey(documentId, branchId), users);
    }

    @Override
    public Set<Long> getConnectedUsers(UUID documentId, UUID branchId) {
        var cache = cacheManager.getCache(DOCUMENT_BRANCH_USERS);

        if (cache == null)
            return new HashSet<>();

        var users = objectMapper.convertValue(
                cache.get(getCacheKey(documentId, branchId), Object.class),
                new TypeReference<Set<Long>>() {
                }
        );

        if (users == null)
            return new HashSet<>();

        return users;
    }

    @Override
    public void setConnectedSession(UUID documentId, UUID branchId, String sessionId, Long userId) {
        var cache = cacheManager.getCache(DOCUMENT_BRANCH_SESSIONS);

        if (cache == null)
            return;

        var sessions = objectMapper.convertValue(
                cache.get("sessions", Object.class),
                new TypeReference<HashMap<String, ConnectedUser>>() {
                }
        );

        if (sessions == null)
            sessions = new HashMap<>();

        var connectedUser = new ConnectedUser(userId, documentId, branchId);

        sessions.entrySet().removeIf(entry -> entry.getValue().equals(connectedUser));

        sessions.put(sessionId, connectedUser);

        cache.put("sessions", sessions);
    }

    @Override
    public void removeDisconnectedUser(ConnectedUser user) {
        var cache = cacheManager.getCache(DOCUMENT_BRANCH_USERS);

        if (cache == null)
            return;

        var users = objectMapper.convertValue(
                cache.get(getCacheKey(user.getDocumentId(), user.getBranchId()), Object.class),
                new TypeReference<Set<Long>>() {
                }
        );

        if (users == null)
            return;

        users.removeIf(userId -> Objects.equals(userId, user.getUserId()));

        cache.put(getCacheKey(user.getDocumentId(), user.getBranchId()), users);
    }

    @Override
    public ConnectedUser removeDisconnectedSession(String sessionId) {
        var cache = cacheManager.getCache(DOCUMENT_BRANCH_SESSIONS);

        if (cache == null)
            return null;

        var sessions = objectMapper.convertValue(
                cache.get("sessions", Object.class),
                new TypeReference<HashMap<String, ConnectedUser>>() {
                }
        );

        if (sessions != null && sessions.containsKey(sessionId)) {
            var user = sessions.get(sessionId);

            removeDisconnectedUser(user);
            sessions.remove(sessionId);

            cache.put("sessions", sessions);

            return user;
        }

        return null;
    }

    private String getCacheKey(UUID documentId, UUID branchId) {
        return documentId + ":" + branchId;
    }
}

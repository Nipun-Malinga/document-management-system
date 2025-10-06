package com.nipun.system.document.websocket.connection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class ConnectionCacheServiceImpl implements ConnectionCacheService {

    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${cache.names.document.websocket.branch-connection.users}")
    private String DOCUMENT_BRANCH_USERS;
    @Value("${cache.names.document.websocket.branch-connection.sessions}")
    private String DOCUMENT_BRANCH_SESSIONS;
    @Value("${cache.names.document.websocket.full-connection}")
    private String DOCUMENT_ALL_USERS;
    @Value("${cache.names.document.websocket.cache-ttl}")
    private int CACHE_EXPIRE_TTL;

    @Override
    public void setDocumentAllConnectedUsers(UUID documentId, UUID branchId, Long userId) {
        var cache = cacheManager.getCache(DOCUMENT_ALL_USERS);

        if (cache == null)
            return;

        var users = objectMapper.convertValue(
                cache.get(documentId, Object.class),
                new TypeReference<Map<String, Set<Long>>>() {
                }
        );

        if (users == null)
            users = new HashMap<>();

        var connections = users.get(branchId.toString());

        if (connections == null)
            connections = new HashSet<>();

        connections.add(userId);

        users.put(branchId.toString(), connections);

        cache.put(documentId, users);

        refreshTTL(DOCUMENT_ALL_USERS, documentId.toString());
    }

    @Override
    public Set<Long> getDocumentAllConnectedUsers(UUID documentId) {
        var cache = cacheManager.getCache(DOCUMENT_ALL_USERS);

        if (cache == null)
            return new HashSet<>();

        var branches = objectMapper.convertValue(
                cache.get(documentId, Object.class),
                new TypeReference<Map<String, Set<Long>>>() {
                }
        );

        if (branches == null)
            return new HashSet<>();

        Set<Long> users = new HashSet<>();

        for (var branch : branches.entrySet()) {
            users.addAll(branch.getValue());
        }

        refreshTTL(DOCUMENT_ALL_USERS, documentId.toString());

        return users;
    }

    @Override
    public void removeDocumentDisconnectedUser(UUID documentId, UUID branchId, Long userId) {
        var cache = cacheManager.getCache(DOCUMENT_ALL_USERS);

        if (cache == null)
            return;

        var branches = objectMapper.convertValue(
                cache.get(documentId, Object.class),
                new TypeReference<Map<String, Set<Long>>>() {
                }
        );

        if (branches == null)
            return;

        var branch = branches.get(branchId.toString());

        if (branch == null) {
            return;
        }

        branch.remove(userId);

        branches.put(branchId.toString(), branch);

        cache.put(documentId, branches);

        refreshTTL(DOCUMENT_ALL_USERS, documentId.toString());
    }

    @Override
    public void setBranchConnectedUser(UUID documentId, UUID branchId, Long userId) {
        var cache = cacheManager.getCache(DOCUMENT_BRANCH_USERS);

        if (cache == null)
            return;

        var cacheKey = getCacheKey(documentId, branchId);

        var users = objectMapper.convertValue(
                cache.get(cacheKey, Object.class),
                new TypeReference<Set<Long>>() {
                }
        );

        if (users == null)
            users = new HashSet<>();

        users.add(userId);

        cache.put(cacheKey, users);

        refreshTTL(DOCUMENT_BRANCH_USERS, cacheKey);
    }

    @Override
    public Set<Long> getBranchConnectedUsers(UUID documentId, UUID branchId) {
        var cache = cacheManager.getCache(DOCUMENT_ALL_USERS);

        if (cache == null)
            return new HashSet<>();

        var users = objectMapper.convertValue(
                cache.get(documentId.toString(), Object.class),
                new TypeReference<Map<String, Set<Long>>>() {
                }
        );

        if (users != null && users.containsKey(branchId.toString()))
            return users.get(branchId.toString());

        refreshTTL(DOCUMENT_ALL_USERS, documentId.toString());

        return new HashSet<>();
    }

    @Override
    public void setBranchConnectedSession(UUID documentId, UUID branchId, String sessionId, Long userId) {
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

        refreshTTL(DOCUMENT_BRANCH_SESSIONS, "sessions");
    }

    @Override
    public ConnectedUser removeBranchDisconnectedSession(String sessionId) {
        var cache = cacheManager.getCache(DOCUMENT_BRANCH_SESSIONS);

        if (cache == null)
            return null;

        var sessions = objectMapper.convertValue(
                cache.get("sessions", Object.class),
                new TypeReference<HashMap<String, ConnectedUser>>() {
                }
        );

        refreshTTL(DOCUMENT_BRANCH_SESSIONS, "sessions");

        if (sessions != null && sessions.containsKey(sessionId)) {
            var user = sessions.get(sessionId);

            removeDocumentDisconnectedUser(user.getDocumentId(), user.getBranchId(), user.getUserId());
            sessions.remove(sessionId);

            cache.put("sessions", sessions);

            return user;
        }

        return null;
    }

    private String getCacheKey(UUID documentId, UUID branchId) {
        return documentId + ":" + branchId;
    }

    private void refreshTTL(String cacheName, String key) {
        String redisKey = cacheName + "::" + key;
        redisTemplate.expire(redisKey, CACHE_EXPIRE_TTL, TimeUnit.MINUTES);
    }
}

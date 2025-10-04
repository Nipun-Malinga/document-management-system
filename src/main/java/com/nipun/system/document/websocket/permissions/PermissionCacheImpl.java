package com.nipun.system.document.websocket.permissions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PermissionCacheImpl implements PermissionCacheService {

    private static final String USER_PERMISSION = "DOCUMENT_USER_PERMISSION_CACHE";
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    @Override
    public void setUserPermissions(UUID documentId, Long userId, Permissions options) {

        var cache = cacheManager.getCache(USER_PERMISSION);

        if (cache == null)
            return;
        var permissions = objectMapper.convertValue(
                cache.get(documentId, Object.class),
                new TypeReference<Map<String, Permissions>>() {
                }
        );

        if (permissions == null)
            permissions = new HashMap<>();

        permissions.put(userId.toString(), options);

        cache.put(documentId, permissions);
    }

    @Override
    public Permissions getUserPermissions(UUID documentId, Long userId) {
        var cache = cacheManager.getCache(USER_PERMISSION);

        if (cache == null)
            return null;

        var permissions = objectMapper.convertValue(
                cache.get(documentId, Object.class),
                new TypeReference<Map<String, Permissions>>() {
                }
        );

        if (permissions == null)
            permissions = new HashMap<>();

        return permissions.get(userId.toString());
    }

    @Override
    public void removePermission(UUID documentId, Long userId) {
        var cache = cacheManager.getCache(USER_PERMISSION);

        if (cache == null)
            return;

        var permissions = objectMapper.convertValue(
                cache.get(documentId, Object.class),
                new TypeReference<Map<String, Permissions>>() {
                }
        );

        if (permissions == null)
            return;

        permissions.remove(userId.toString());

        cache.put(documentId, permissions);
    }
}

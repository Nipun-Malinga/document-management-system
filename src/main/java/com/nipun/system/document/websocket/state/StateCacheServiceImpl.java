package com.nipun.system.document.websocket.state;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nipun.system.document.websocket.utils.CacheUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class StateCacheServiceImpl implements StateCacheService {

    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;
    private final CacheUtils cacheUtils;

    @Value("${cache.names.document.websocket.state}")
    private String STATE;
    @Value("${cache.names.document.websocket.cache-ttl}")
    private int CACHE_EXPIRE_TTL;

    @Override
    public void setDocumentState(UUID documentId, UUID branchId, String content) {
        var cache = cacheManager.getCache(STATE);

        if (cache == null)
            return;

        var cacheKey = cacheUtils.generateCacheKey(documentId, branchId);

        cache.put(cacheKey, content);

        cacheUtils.refreshTTL(STATE, cacheKey, CACHE_EXPIRE_TTL);
    }

    @Override
    public String getDocumentState(UUID documentId, UUID branchId) {
        var cache = cacheManager.getCache(STATE);

        if (cache == null)
            return null;

        var cacheKey = cacheUtils.generateCacheKey(documentId, branchId);

        var state = cache.get(cacheKey, Object.class);

        if (state == null) return null;

        cacheUtils.refreshTTL(STATE, cacheKey, CACHE_EXPIRE_TTL);

        return objectMapper.convertValue(state, new TypeReference<>() {
        });
    }
}

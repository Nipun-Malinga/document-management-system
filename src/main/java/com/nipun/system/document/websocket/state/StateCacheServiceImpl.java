package com.nipun.system.document.websocket.state;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    @Value("${cache.names.document.websocket.state}")
    private String STATE;

    @Override
    public void setDocumentState(UUID documentId, UUID branchId, String content) {
        var cache = cacheManager.getCache(STATE);

        if (cache == null)
            return;

        cache.put(getCacheKey(documentId, branchId), content);
    }

    @Override
    public String getDocumentState(UUID documentId, UUID branchId) {
        var cache = cacheManager.getCache(STATE);

        if (cache == null)
            return null;

        var state = cache.get(getCacheKey(documentId, branchId), Object.class);

        if (state == null) return null;

        return objectMapper.convertValue(state, new TypeReference<>() {
        });
    }

    private String getCacheKey(UUID documentId, UUID branchId) {
        return documentId + ":" + branchId;
    }
}

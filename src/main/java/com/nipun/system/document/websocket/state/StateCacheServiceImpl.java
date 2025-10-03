package com.nipun.system.document.websocket.state;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class StateCacheServiceImpl implements StateCacheService {

    private static final String STATUS = "DOCUMENT_STATUS_CACHE";

    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    @Override
    public void setDocumentState(UUID documentId, UUID branchId, String content) {
        var cache = cacheManager.getCache(STATUS);

        if (cache == null)
            return;

        cache.put(getCacheKey(documentId, branchId), content);
    }

    @Override
    public String getDocumentState(UUID documentId, UUID branchId) {
        var cache = cacheManager.getCache(STATUS);

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

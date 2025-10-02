package com.nipun.system.document.websocket.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WebsocketRedisCacheServiceImpl implements WebsocketCacheService {

    private static final String USER_PERMISSION = "DOCUMENT_USER_PERMISSION_CACHE";
    private static final String STATUS = "DOCUMENT_STATUS_CACHE";
    private static final String SESSION = "DOCUMENT_SESSION_CACHE";
    private static final String CONNECTED_USERS = "DOCUMENT_CONNECTED_USERS_CACHE";

    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

//    return objectMapper.convertValue(value, new TypeReference<>() {});
}

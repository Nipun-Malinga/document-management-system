package com.nipun.system.document.websocket;

import com.nipun.system.document.DocumentRepository;
import com.nipun.system.document.exceptions.DocumentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class DocumentWebSocketService {

    private final DocumentRepository documentRepository;
    private final CacheManager cacheManager;

    public Boolean isAuthorizedUser(Long userId, UUID documentId) {
        var permissionCache = cacheManager.getCache("USER_PERMISSION_CACHE");

        String cacheKey = documentId.toString() + ":" + userId;

        if (permissionCache != null && Boolean.TRUE.equals(permissionCache.get(cacheKey, Boolean.class)))
            return true;

        var document = documentRepository.findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        boolean authorized = !document.isUnauthorizedUser(userId)
                && !document.isReadOnlyUser(userId);

        if (permissionCache != null)
            permissionCache.put(cacheKey, authorized);

        return authorized;
    }

    public void setDocumentStatus(UUID documentId, String status) {
        var cache = cacheManager.getCache("DOCUMENT_STATUS_CACHE");

        if (cache != null) {
            cache.put(documentId, status);
        }
    }

    public String getDocumentStatusFromCache(UUID documentId) {
        var cache = cacheManager.getCache("DOCUMENT_STATUS_CACHE");

        if(cache != null && cache.get(documentId) != null) {
            return cache.get(documentId, String.class);
        }

        var document = documentRepository.findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        var content = document.getContent().getContent();

        if(cache != null && content != null) {
            cache.put(documentId, content);
            return content;
        }

        return null;
    }
}

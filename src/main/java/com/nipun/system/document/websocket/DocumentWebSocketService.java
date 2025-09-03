package com.nipun.system.document.websocket;

import com.nipun.system.document.DocumentRepository;
import com.nipun.system.document.exceptions.DocumentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
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

    public void addConnectedUserToCache(UUID documentId, String sessionId, Long userId) {
        var userCache = cacheManager.getCache("DOCUMENT_USER_CACHE");

        if(userCache == null) return;

        if(userCache.get(sessionId, ConnectedUser.class) == null)
            userCache.put(sessionId, new ConnectedUser(userId, documentId));

        var documentSessionCache = cacheManager.getCache("DOCUMENT_SESSION_CACHE");

        if(documentSessionCache == null) return;

        Set<Long> connectedUsers = documentSessionCache.get(documentId, HashSet.class);

        if(connectedUsers == null) {
            connectedUsers = new HashSet<>();
            documentSessionCache.put(documentId, connectedUsers);
        }

        connectedUsers.add(userId);

        documentSessionCache.put(documentId, connectedUsers);
    }

    public UUID removeDisconnectedUserFromCache(String sessionId) {
        var userCache = cacheManager.getCache("DOCUMENT_USER_CACHE");

        if(userCache == null) return null;

        var documentSessionCache = cacheManager.getCache("DOCUMENT_SESSION_CACHE");

        if(documentSessionCache == null) return null;

        var user = userCache.get(sessionId, ConnectedUser.class);

        userCache.evict(sessionId);

        if(user != null) {
            Set<Long> connectedUsers = documentSessionCache.get(user.getDocumentId(), HashSet.class);

            if(connectedUsers != null)
                connectedUsers.remove(user.getUserId());

            documentSessionCache.put(user.getDocumentId(), connectedUsers);
            return user.getDocumentId();
        }

        return null;
    }

    public ConnectedUsers getConnectedUsers(UUID documentId) {
        var documentSessionCache = cacheManager.getCache("DOCUMENT_SESSION_CACHE");

        if(documentSessionCache != null) {
            return new ConnectedUsers(documentId, documentSessionCache.get(documentId, HashSet.class));
        }

        return null;
    }

}

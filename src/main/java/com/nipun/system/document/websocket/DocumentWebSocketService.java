package com.nipun.system.document.websocket;

import com.nipun.system.document.DocumentRepository;
import com.nipun.system.document.exceptions.DocumentNotFoundException;
import com.nipun.system.shared.entities.WebsocketPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
public class DocumentWebSocketService {

    private final DocumentRepository documentRepository;
    private final CacheManager cacheManager;

    public boolean isUnauthorizedUser(Long userId, UUID documentId) {
        var permissionCache = cacheManager.getCache("DOCUMENT_USER_PERMISSION_CACHE");

        /*
            Converting the user id to string because redis
            cache only supports string keys.
        */
        var userIdCacheKey = userId.toString();

        if (permissionCache == null) return true;

        Map<String, AuthorizedOptions> userPermissions = permissionCache.get(documentId, Map.class);

        if (userPermissions != null && userPermissions.containsKey(userIdCacheKey)) {
            return userPermissions.get(userIdCacheKey).isAuthorizedUser();
        }

        var document = documentRepository.findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        if (userPermissions == null) {
            userPermissions = new ConcurrentHashMap<>();
        }

        var isUnauthorized = document.isUnauthorizedUser(userId);
        var isReadOnlyUser= document.isReadOnlyUser(userId);

        userPermissions.put(userIdCacheKey, new AuthorizedOptions(isUnauthorized, isReadOnlyUser));
        permissionCache.put(documentId, userPermissions);

        return isUnauthorized;
    }

    public boolean isReadOnlyUser(Long userId, UUID documentId) {
        var permissionCache = cacheManager.getCache("DOCUMENT_USER_PERMISSION_CACHE");
        var userIdKey = userId.toString();

        if (permissionCache == null) return false;

        Map<String, AuthorizedOptions> userPermissions = permissionCache.get(documentId, Map.class);

        return userPermissions != null &&
               userPermissions.containsKey(userIdKey) &&
               userPermissions.get(userIdKey).isReadOnlyUser();
    }

    public void setDocumentStatus(UUID documentId, String status) {
        var cache = cacheManager.getCache("DOCUMENT_STATUS_CACHE");

        if (cache != null) {
            cache.put(documentId, status);
        }
    }

    public String getDocumentStatusFromCache(UUID documentId) {
        var cache = cacheManager.getCache("DOCUMENT_STATUS_CACHE");

        if (cache != null) {
            String cachedContent = cache.get(documentId, String.class);
            if (cachedContent != null) {
                return cachedContent;
            }
        }

        var document = documentRepository.findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        var content = document.getContent().getContent();

        if (cache != null) {
            cache.put(documentId, content == null ? "" : content);
        }

        return content == null ? "" : content;
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

        if(documentSessionCache == null || documentSessionCache.get(documentId) == null) return null;

        return new ConnectedUsers(documentId, documentSessionCache.get(documentId, HashSet.class));
    }

    public void saveDocumentState(UUID documentId) {
        var cache = cacheManager.getCache("DOCUMENT_STATUS_CACHE");

        if(cache == null) return;

        var currentState = cache.get(documentId, String.class);

        if(currentState == null) return;

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        document.addContent(currentState);
        documentRepository.save(document);
    }

    /**
     * <p style="color:green">Broadcasts the connected users.</p>
     * <p style="color:green">Saves the current change if there are no users.</p>
    */
    public WebsocketPayload<Set<Long>> getDocumentConnectedUsers(String sessionId) {
        var documentId = removeDisconnectedUserFromCache(sessionId);
        var connectedUsers = getConnectedUsers(documentId);

        if(connectedUsers != null) {
            if(connectedUsers.getUsers().isEmpty())
                saveDocumentState(documentId);

            return new WebsocketPayload<>("/document/" + documentId + "/broadcastUsers",
                    connectedUsers.getUsers());
        }

        return null;
    }

    public void updateDocumentPermissionDetails(UUID documentId, Long userId, AuthorizedOptions authorizedOptions) {
        var permissionCache = cacheManager.getCache("DOCUMENT_USER_PERMISSION_CACHE");

        var userIdCacheKey = userId.toString();

        if (permissionCache == null) return;

        Map<String, AuthorizedOptions> userPermissions = permissionCache.get(documentId, Map.class);

        if (userPermissions != null && userPermissions.containsKey(userIdCacheKey)) {
            userPermissions.put(userIdCacheKey, authorizedOptions);
            permissionCache.put(documentId, userPermissions);
        }
    }

    public void removeDocumentPermissionDetailsFromCache(UUID documentId, Long userId) {
        var permissionCache = cacheManager.getCache("DOCUMENT_USER_PERMISSION_CACHE");

        if (permissionCache == null) return;

        // Converting the user id to string because redis cache only supports string keys.
        var userIdCacheKey = userId.toString();

        Map<String, AuthorizedOptions> userPermissions = permissionCache.get(documentId, Map.class);

        if (userPermissions != null) {
            userPermissions.remove(userIdCacheKey);
            permissionCache.put(documentId, userPermissions);
        }
    }
}

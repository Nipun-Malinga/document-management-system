package com.nipun.system.document.websocket;

import com.nipun.system.document.DocumentRepository;
import com.nipun.system.document.cache.DocumentCacheService;
import com.nipun.system.document.exceptions.DocumentNotFoundException;
import com.nipun.system.document.share.SharedDocumentAuthService;
import com.nipun.system.shared.entities.WebsocketPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
public class DocumentWebSocketService {

    private final DocumentRepository documentRepository;
    private final DocumentCacheService documentCacheService;
    private final SharedDocumentAuthService sharedDocumentAuthService;

    public boolean isUnauthorizedUser(Long userId, UUID documentId) {
        var userIdCacheKey = userId.toString();

        Map<String, AuthorizedOptions> userPermissions = documentCacheService
                .getDocumentUserPermissions(documentId);

        if (userPermissions != null && userPermissions.containsKey(userIdCacheKey)) {
            return userPermissions.get(userIdCacheKey).isUnAuthorizedUser();
        }

        var document = documentRepository.findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        if (userPermissions == null) {
            userPermissions = new ConcurrentHashMap<>();
        }

        var isUnauthorized = sharedDocumentAuthService.isUnauthorizedUser(userId, document);
        var isReadOnlyUser = sharedDocumentAuthService.isReadOnlyUser(userId, document);

        userPermissions.put(userIdCacheKey, new AuthorizedOptions(isUnauthorized, isReadOnlyUser));

        documentCacheService.putDocumentUserPermissions(documentId, userPermissions);

        return isUnauthorized;
    }

    public boolean isReadOnlyUser(Long userId, UUID documentId) {
        var userIdKey = userId.toString();

        Map<String, AuthorizedOptions> userPermissions = documentCacheService
                .getDocumentUserPermissions(documentId);

        return userPermissions != null &&
               userPermissions.containsKey(userIdKey) &&
               userPermissions.get(userIdKey).isReadOnlyUser();
    }

    public void setDocumentStatus(UUID documentId, String status) {
        documentCacheService.putDocumentCurrentStatus(documentId, status);
    }

    public String getDocumentStatusFromCache(UUID documentId) {
        String cachedContent = documentCacheService.getDocumentCurrentStatus(documentId);
        if (cachedContent != null) {
                return cachedContent;
        }

        var document = documentRepository.findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        var content = document.getDocumentContent() == null ? "" : document.getDocumentContent();

        documentCacheService.putDocumentCurrentStatus(documentId, content);

        return content;
    }

    public void saveDocumentState(UUID documentId) {

        var currentState = documentCacheService.getDocumentCurrentStatus(documentId);

        if(currentState == null) return;

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        document.addContent(currentState);
        documentRepository.save(document);
    }

    public void addConnectedUserToCache(UUID documentId, String sessionId, Long userId) {

        if(documentCacheService.getDocumentConnectedUsers(sessionId) == null)
            documentCacheService.putDocumentConnectedUsers(
                    sessionId, new ConnectedUser(userId, documentId));

        Set<Long> connectedUsers = documentCacheService
                .getDocumentConnectedSessions(documentId);

        if(connectedUsers == null)
            connectedUsers = new HashSet<>();

        connectedUsers.add(userId);

        documentCacheService
                .putDocumentConnectedSession(documentId, connectedUsers);
    }

    public UUID removeDisconnectedUserFromCache(String sessionId) {

        var user = documentCacheService.getDocumentConnectedUsers(sessionId);

        documentCacheService.removeDocumentConnectedSession(sessionId);

        if(user != null) {
            Set<Long> connectedUsers = documentCacheService
                    .getDocumentConnectedSessions(user.getDocumentId());

            if(connectedUsers != null)
                connectedUsers.remove(user.getUserId());

            documentCacheService.putDocumentConnectedSession(user.getDocumentId(), connectedUsers);
            return user.getDocumentId();
        }

        return null;
    }

    public ConnectedUsers getConnectedUsers(UUID documentId) {

        var connectedUsers = documentCacheService
                .getDocumentConnectedSessions(documentId);

        if(connectedUsers == null) return null;

        return new ConnectedUsers(documentId, connectedUsers);
    }

    /**
     * <p style="color:green">Broadcasts the connected users.</p>
     * <p style="color:green">Saves the current change if there are no users.</p>
    */
    public WebsocketPayload<Set<Long>> getConnectedUsers(String sessionId) {
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

        var userIdCacheKey = userId.toString();

        Map<String, AuthorizedOptions> userPermissions = documentCacheService
                .getDocumentUserPermissions(documentId);

        if (userPermissions != null && userPermissions.containsKey(userIdCacheKey)) {
            userPermissions.put(userIdCacheKey, authorizedOptions);
            documentCacheService.putDocumentUserPermissions(documentId, userPermissions);
        }
    }

    public void removeDocumentPermissionDetailsFromCache(UUID documentId, Long userId) {

        var userIdCacheKey = userId.toString();

        Map<String, AuthorizedOptions> userPermissions = documentCacheService
                .getDocumentUserPermissions(documentId);

        if (userPermissions != null && userPermissions.containsKey(userIdCacheKey)) {
            userPermissions.remove(userIdCacheKey);
            documentCacheService.putDocumentUserPermissions(documentId, userPermissions);
        }
    }
}

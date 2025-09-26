package com.nipun.system.document.websocket;

import com.nipun.system.shared.entities.WebsocketPayload;

import java.util.Set;
import java.util.UUID;

public interface DocumentWebsocketService {
    boolean isUnauthorizedUser(Long userId, UUID documentId);

    boolean isReadOnlyUser(Long userId, UUID documentId);

    void setDocumentStatus(UUID documentId, String status);

    String getDocumentStatusFromCache(UUID documentId);

    void saveDocumentState(UUID documentId);

    void addConnectedUserToCache(UUID documentId, String sessionId, Long userId);

    UUID removeDisconnectedUserFromCache(String sessionId);

    ConnectedUsers getConnectedUsers(UUID documentId);

    WebsocketPayload<Set<Long>> getConnectedUsers(String sessionId);

    void updateDocumentPermissionDetails(UUID documentId, Long userId, AuthorizedOptions authorizedOptions);

    void removeDocumentPermissionDetailsFromCache(UUID documentId, Long userId);
}

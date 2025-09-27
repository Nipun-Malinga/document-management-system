package com.nipun.system.document.cache;

import com.nipun.system.document.websocket.authentication.AuthorizedOptions;
import com.nipun.system.document.websocket.connection.ConnectedUser;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface DocumentCacheService {

    Map<String, AuthorizedOptions> getDocumentUserPermissions(UUID documentId);

    void putDocumentUserPermissions(UUID documentId, Map<String, AuthorizedOptions> permissions);

    String getDocumentCurrentStatus(UUID documentId);

    void putDocumentCurrentStatus(UUID documentId,  String status);

    Set<Long> getDocumentConnectedSessions(UUID documentId);

    void putDocumentConnectedSession(UUID documentId, Set<Long> sessions);

    void removeDocumentConnectedSession(String sessionId);

    ConnectedUser getDocumentConnectedUsers(String sessionId);

    void putDocumentConnectedUsers(String sessionId, ConnectedUser connectedUser);
}

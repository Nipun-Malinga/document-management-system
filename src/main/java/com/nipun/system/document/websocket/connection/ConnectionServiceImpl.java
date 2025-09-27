package com.nipun.system.document.websocket.connection;

import com.nipun.system.document.cache.DocumentCacheService;
import com.nipun.system.document.websocket.state.StateService;
import com.nipun.system.shared.entities.WebsocketPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ConnectionServiceImpl implements ConnectionService {
    private final DocumentCacheService documentCacheService;
    private final StateService stateService;

    @Override
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

    @Override
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

    @Override
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
    @Override
    public WebsocketPayload<Set<Long>> getConnectedUsers(String sessionId) {
        var documentId = removeDisconnectedUserFromCache(sessionId);
        var connectedUsers = getConnectedUsers(documentId);

        if(connectedUsers != null) {
            if(connectedUsers.getUsers().isEmpty())
                stateService.saveDocumentState(documentId);

            return new WebsocketPayload<>("/document/" + documentId + "/broadcastUsers",
                    connectedUsers.getUsers());
        }

        return null;
    }
}

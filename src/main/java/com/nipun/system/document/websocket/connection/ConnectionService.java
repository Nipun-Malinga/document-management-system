package com.nipun.system.document.websocket.connection;

import com.nipun.system.shared.entities.WebsocketPayload;

import java.util.Set;
import java.util.UUID;

public interface ConnectionService {
    void addConnectedUserToCache(UUID documentId, String sessionId, Long userId);
    UUID removeDisconnectedUserFromCache(String sessionId);
    ConnectedUsers getConnectedUsers(UUID documentId);
    WebsocketPayload<Set<Long>> getConnectedUsers(String sessionId);
}

package com.nipun.system.document.websocket.connection;

import java.util.Set;
import java.util.UUID;

public interface ConnectionService {
    void registerConnectedUser(UUID documentId, UUID branchId, String sessionId, Long userId);

    void removeDisconnectedSession(String sessionId);

    Set<Long> getAllConnectedUsers(UUID documentId);

    Set<Long> getConnectedUsers(UUID documentId, UUID branchId);
}

package com.nipun.system.document.websocket.connection;

import java.util.Set;
import java.util.UUID;

public interface ConnectionCacheService {
    void setConnectedUser(UUID documentId, UUID branchId, Long userId);

    Set<Long> getConnectedUsers(UUID documentId, UUID branchId);

    void setConnectedSession(UUID documentId, UUID branchId, String sessionId, Long userId);

    void removeDisconnectedUser(ConnectedUser user);

    ConnectedUser removeDisconnectedSession(String sessionId);

}

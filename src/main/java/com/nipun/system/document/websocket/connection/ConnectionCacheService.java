package com.nipun.system.document.websocket.connection;

import java.util.Set;
import java.util.UUID;

public interface ConnectionCacheService {
    void setDocumentAllConnectedUsers(UUID documentId, UUID branchId, Long userId);

    Set<Long> getDocumentAllConnectedUsers(UUID documentId);

    void removeDocumentDisconnectedUser(UUID documentId, UUID branchId, Long userId);

    void setBranchConnectedUser(UUID documentId, UUID branchId, Long userId);

    Set<Long> getBranchConnectedUsers(UUID documentId, UUID branchId);

    void setBranchConnectedSession(UUID documentId, UUID branchId, String sessionId, Long userId);

    void removeBranchDisconnectedUser(ConnectedUser user);

    ConnectedUser removeBranchDisconnectedSession(String sessionId);

}

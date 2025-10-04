package com.nipun.system.document.websocket.connection;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ConnectionServiceImpl implements ConnectionService {

    private final ConnectionCacheService cacheService;
    private final SimpMessagingTemplate messagingTemplate;


    @Override
    public void registerConnectedUser(UUID documentId, UUID branchId, String sessionId, Long userId) {
        cacheService.setConnectedSession(documentId, branchId, sessionId, userId);
        cacheService.setConnectedUser(documentId, branchId, userId);

        messagingTemplate.convertAndSend(
                "/document/" + documentId + "/branch/" + branchId + "/users",
                getConnectedUsers(documentId, branchId)
        );
    }

    @Override
    public void removeDisconnectedSession(String sessionId) {

        var user = cacheService.removeDisconnectedSession(sessionId);

        if (user != null)
            messagingTemplate.convertAndSend(
                    "/document/" + user.getDocumentId() + "/branch/" + user.getBranchId() + "/users",
                    getConnectedUsers(user.getDocumentId(), user.getBranchId())
            );
    }

    @Override
    public Set<Long> getAllConnectedUsers(UUID documentId) {
        return Set.of();
    }

    @Override
    public Set<Long> getConnectedUsers(UUID documentId, UUID branchId) {
        return cacheService.getConnectedUsers(documentId, branchId);
    }
}

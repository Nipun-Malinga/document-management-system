package com.nipun.system.document.websocket.connection;

import com.nipun.system.document.share.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.websocket.permissions.PermissionService;
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
    private final PermissionService permissionService;

    @Override
    public void registerConnectedUser(UUID documentId, UUID branchId, String sessionId, Long userId) {

        if (permissionService.isUnauthorizedUser(documentId, userId))
            throw new UnauthorizedDocumentException();

        cacheService.setBranchConnectedSession(documentId, branchId, sessionId, userId);
        cacheService.setBranchConnectedUser(documentId, branchId, userId);
        cacheService.setDocumentAllConnectedUsers(documentId, branchId, userId);

        messagingTemplate.convertAndSend(
                "/document/" + documentId + "/branch/" + branchId + "/users",
                getConnectedUsers(documentId, branchId)
        );

        messagingTemplate.convertAndSend(
                "/document/" + documentId + "/users",
                getAllConnectedUsers(documentId)
        );
    }

    @Override
    public void removeDisconnectedSession(String sessionId) {

        var user = cacheService.removeBranchDisconnectedSession(sessionId);

        if (user != null) {
            cacheService.removeDocumentDisconnectedUser(user.getDocumentId(), user.getBranchId(), user.getUserId());

            messagingTemplate.convertAndSend(
                    "/document/" + user.getDocumentId() + "/branch/" + user.getBranchId() + "/users",
                    getConnectedUsers(user.getDocumentId(), user.getBranchId())
            );

            messagingTemplate.convertAndSend(
                    "/document/" + user.getDocumentId() + "/users",
                    getAllConnectedUsers(user.getDocumentId())
            );
        }
    }

    @Override
    public Set<Long> getAllConnectedUsers(UUID documentId) {
        return cacheService.getDocumentAllConnectedUsers(documentId);
    }

    @Override
    public Set<Long> getConnectedUsers(UUID documentId, UUID branchId) {
        return cacheService.getBranchConnectedUsers(documentId, branchId);
    }
}

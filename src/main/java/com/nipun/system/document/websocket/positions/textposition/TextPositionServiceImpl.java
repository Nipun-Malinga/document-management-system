package com.nipun.system.document.websocket.positions.textposition;

import com.nipun.system.document.share.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.websocket.permissions.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TextPositionServiceImpl implements TextPositionService {

    private final PermissionService permissionService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcastUserSelectPositions(
            UUID documentId,
            UUID branchId,
            Long userId,
            TextPosition textPosition
    ) {

        if (permissionService.isUnauthorizedUser(documentId, userId))
            throw new UnauthorizedDocumentException();

        var endpoint = "/document/" + documentId + "/branch/" + branchId + "/user/" + userId + "/broadcast-selected-positions";
        messagingTemplate.convertAndSend(endpoint, textPosition);
    }
}

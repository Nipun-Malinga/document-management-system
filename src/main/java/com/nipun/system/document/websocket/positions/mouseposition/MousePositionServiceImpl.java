package com.nipun.system.document.websocket.positions.mouseposition;

import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class MousePositionServiceImpl implements MousePositionService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcastUserMousePositions(
            UUID documentId,
            UUID branchId,
            Long userId,
            MousePosition position
    ) {
        var endpoint = "/document/" + documentId + "/branch/" + branchId + "/user/" + userId + "/broadcast-mouse-positions";
        messagingTemplate.convertAndSend(endpoint, position);
    }
}
